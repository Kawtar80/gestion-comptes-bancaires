package com.example.bank.service;

import com.example.bank.model.BankAccount;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

@Service
public class BankAgentService {

    private final ChatClient chatClient;
    private final BankAccountService bankAccountService;

    public BankAgentService(ChatClient.Builder chatClientBuilder, BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
        this.chatClient = chatClientBuilder
                .defaultSystem(
                        "You are an automated bank agent. You have tools to execute operations (depositMoney, withdrawMoney, listAccounts, etc.). "
                                +
                                "IMPORTANT: Because of a system limitation, to call a tool you MUST reply EXACTLY with this text format: 'FUNCTION: depositMoney(id, amount)'. "
                                +
                                "Example: If user wants to deposit 500 on account 1, reply ONLY: 'FUNCTION: depositMoney(1, 500)'. "
                                +
                                "Do not add any other text. If you don't have the ID or amount, ask the user in French.")
                .defaultFunctions("listAccounts", "depositMoney", "withdrawMoney", "createAccount", "deleteAccount")
                .build();
    }

    @SuppressWarnings("null")
    public AgentResponse processCommand(String command) {
        try {
            // LAYER 1: DETERMINISTIC INTENT ROUTER (Fast-Path)
            // Assure une fiabilité de 100% pour les commandes claires, contournant les
            // hallucinations du modèle local Mistral 7B
            String lowerCmd = command.toLowerCase();

            if ((lowerCmd.contains("dépôt") || lowerCmd.contains("depot")) && command.matches(".*\\d+.*\\d+.*")) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)[^\\d]+(\\d+)").matcher(lowerCmd);
                if (m.find()) {
                    long val1 = Long.parseLong(m.group(1));
                    double val2 = Double.parseDouble(m.group(2));
                    long accountId = (val1 < 100) ? val1 : (long) val2; // On suppose que l'ID est le petit nombre
                    double amount = (val1 > 100) ? val1 : val2;
                    bankAccountService.deposit(accountId, amount);
                    return new AgentResponse(
                            "✅ Dépôt de " + amount + "€ effectué avec succès sur le compte " + accountId + ".", true);
                }
            }

            if (lowerCmd.contains("retrait") && command.matches(".*\\d+.*\\d+.*")) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)[^\\d]+(\\d+)").matcher(lowerCmd);
                if (m.find()) {
                    long val1 = Long.parseLong(m.group(1));
                    double val2 = Double.parseDouble(m.group(2));
                    long accountId = (val1 < 100) ? val1 : (long) val2;
                    double amount = (val1 > 100) ? val1 : val2;
                    bankAccountService.withdraw(accountId, amount);
                    return new AgentResponse(
                            "✅ Retrait de " + amount + "€ effectué avec succès sur le compte " + accountId + ".", true);
                }
            }

            if (lowerCmd.contains("liste") && lowerCmd.contains("comptes")) {
                return new AgentResponse(
                        "✅ J'ai rafraîchi l'affichage. Vous pouvez consulter tous vos comptes dans le tableau.", true);
            }

            // LAYER 2: SPRING AI NATIVE FUNCTION CALLING
            // Appel au modèle LLM local pour les phrases complexes ou chit-chat
            String response = chatClient.prompt()
                    .user(command)
                    .call()
                    .content();

            if (response == null) {
                return new AgentResponse("Erreur : aucune réponse de l'IA.", false);
            }

            // LAYER 3: NLP POST-PROCESSING (Fallback)
            // Au cas où le modèle essaie d'exécuter la fonction mais l'écrit en texte au
            // lieu d'utiliser le JSON API
            if (response.contains("depositMoney")
                    || (response.contains("dépôt") && response.matches(".*\\d+.*\\d+.*"))) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)[^\\d]+(\\d+)").matcher(response);
                if (m.find()) {
                    long val1 = Long.parseLong(m.group(1));
                    double val2 = Double.parseDouble(m.group(2));
                    long accountId = (val1 < 100) ? val1 : (long) val2;
                    double amount = (val1 > 100) ? val1 : val2;
                    bankAccountService.deposit(accountId, amount);
                    return new AgentResponse(
                            "✅ Opération comprise ! Dépôt de " + amount + "€ validé sur le compte " + accountId + ".",
                            true);
                }
            }
            if (response.contains("withdrawMoney")
                    || (response.contains("retrait") && response.matches(".*\\d+.*\\d+.*"))) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)[^\\d]+(\\d+)").matcher(response);
                if (m.find()) {
                    long val1 = Long.parseLong(m.group(1));
                    double val2 = Double.parseDouble(m.group(2));
                    long accountId = (val1 < 100) ? val1 : (long) val2;
                    double amount = (val1 > 100) ? val1 : val2;
                    bankAccountService.withdraw(accountId, amount);
                    return new AgentResponse(
                            "✅ Opération comprise ! Retrait de " + amount + "€ validé sur le compte " + accountId + ".",
                            true);
                }
            }

            return new AgentResponse(response, true);
        } catch (Exception e) {
            return new AgentResponse("Erreur de l'agent AI : " + e.getMessage(), false);
        }
    }

    public static class AgentResponse {
        private final String reply;
        private final boolean success;

        public AgentResponse(String reply, boolean success) {
            this.reply = reply;
            this.success = success;
        }

        public String getReply() {
            return reply;
        }

        public boolean isSuccess() {
            return success;
        }
    }

    // --- TOOL DEFINITIONS ---

    @Configuration
    public static class BankAgentTools {

        public record AccountIdRequest(Long accountId) {
        }

        public record AmountRequest(Long accountId, Double amount) {
        }

        public record CreateRequest(String accountHolder) {
        }

        public record NoRequest() {
        }

        @Bean
        @Description("List all available bank accounts with their ID, account holder name, and balance.")
        public Function<NoRequest, String> listAccounts(BankAccountService bankAccountService) {
            return req -> {
                List<BankAccount> accounts = bankAccountService.findAll();
                if (accounts.isEmpty())
                    return "Aucun compte disponible.";
                StringBuilder sb = new StringBuilder("Comptes :\n");
                for (BankAccount a : accounts) {
                    sb.append(String.format("- ID: %d, Titulaire: %s, Solde: %.2f €\n", a.getId(), a.getAccountHolder(),
                            a.getBalance()));
                }
                return sb.toString();
            };
        }

        @Bean
        @Description("Deposit money into an existing bank account using its ID.")
        public Function<AmountRequest, String> depositMoney(BankAccountService bankAccountService) {
            return req -> {
                try {
                    BankAccount account = bankAccountService.deposit(req.accountId(), req.amount());
                    return String.format("Dépôt réussi. Le nouveau solde du compte %d est %.2f €.", account.getId(),
                            account.getBalance());
                } catch (IllegalArgumentException e) {
                    return "Erreur : " + e.getMessage();
                }
            };
        }

        @Bean
        @Description("Withdraw money from an existing bank account using its ID.")
        public Function<AmountRequest, String> withdrawMoney(BankAccountService bankAccountService) {
            return req -> {
                try {
                    BankAccount account = bankAccountService.withdraw(req.accountId(), req.amount());
                    return String.format("Retrait réussi. Le nouveau solde du compte %d est %.2f €.", account.getId(),
                            account.getBalance());
                } catch (IllegalArgumentException e) {
                    return "Erreur : " + e.getMessage();
                }
            };
        }

        @Bean
        @Description("Create a new bank account for the specified account holder name.")
        public Function<CreateRequest, String> createAccount(BankAccountService bankAccountService) {
            return req -> {
                String iban = generateIban();
                BankAccount account = new BankAccount(null, req.accountHolder(), iban, 0.0);
                BankAccount saved = bankAccountService.save(account);
                return String.format("Compte créé avec succès pour %s. ID du compte : %d.", saved.getAccountHolder(),
                        saved.getId());
            };
        }

        @Bean
        @Description("Delete an existing bank account using its ID.")
        public Function<AccountIdRequest, String> deleteAccount(BankAccountService bankAccountService) {
            return req -> {
                try {
                    bankAccountService.findById(req.accountId()); // Verify existence
                    bankAccountService.deleteById(req.accountId());
                    return "Le compte " + req.accountId() + " a été supprimé avec succès.";
                } catch (IllegalArgumentException e) {
                    return "Erreur : " + e.getMessage();
                }
            };
        }

        private String generateIban() {
            Random random = new Random();
            return String.format("FR76 %04d %04d %04d %04d %04d %03d",
                    random.nextInt(10000), random.nextInt(10000),
                    random.nextInt(10000), random.nextInt(10000),
                    random.nextInt(10000), random.nextInt(1000));
        }
    }
}
