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

    public BankAgentService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("Tu es un assistant bancaire compétent. Tu aides les utilisateurs à gérer leurs comptes bancaires. " +
                        "Tu as accès à des outils pour lister les comptes, faire des dépôts, des retraits, créer ou supprimer des comptes. " +
                        "Utilise ces outils dès que l'utilisateur en fait la demande. Réponds de manière concise, polie et en français.")
                .defaultFunctions("listAccounts", "depositMoney", "withdrawMoney", "createAccount", "deleteAccount")
                .build();
    }

    public AgentResponse processCommand(String command) {
        try {
            String response = chatClient.prompt()
                    .user(command)
                    .call()
                    .content();
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

        public String getReply() { return reply; }
        public boolean isSuccess() { return success; }
    }

    // --- TOOL DEFINITIONS ---

    @Configuration
    public static class BankAgentTools {

        public record AccountIdRequest(Long accountId) {}
        public record AmountRequest(Long accountId, Double amount) {}
        public record CreateRequest(String accountHolder) {}
        public record NoRequest() {}

        @Bean
        @Description("Liste tous les comptes bancaires disponibles avec leur ID, nom du titulaire et solde.")
        public Function<NoRequest, String> listAccounts(BankAccountService bankAccountService) {
            return req -> {
                List<BankAccount> accounts = bankAccountService.findAll();
                if (accounts.isEmpty()) return "Aucun compte disponible.";
                StringBuilder sb = new StringBuilder("Comptes :\n");
                for (BankAccount a : accounts) {
                    sb.append(String.format("- ID: %d, Titulaire: %s, Solde: %.2f €\n", a.getId(), a.getAccountHolder(), a.getBalance()));
                }
                return sb.toString();
            };
        }

        @Bean
        @Description("Effectue un dépôt d'argent sur un compte bancaire existant en utilisant son ID.")
        public Function<AmountRequest, String> depositMoney(BankAccountService bankAccountService) {
            return req -> {
                try {
                    BankAccount account = bankAccountService.deposit(req.accountId(), req.amount());
                    return String.format("Dépôt réussi. Le nouveau solde du compte %d est %.2f €.", account.getId(), account.getBalance());
                } catch (IllegalArgumentException e) {
                    return "Erreur : " + e.getMessage();
                }
            };
        }

        @Bean
        @Description("Effectue un retrait d'argent depuis un compte bancaire existant en utilisant son ID.")
        public Function<AmountRequest, String> withdrawMoney(BankAccountService bankAccountService) {
            return req -> {
                try {
                    BankAccount account = bankAccountService.withdraw(req.accountId(), req.amount());
                    return String.format("Retrait réussi. Le nouveau solde du compte %d est %.2f €.", account.getId(), account.getBalance());
                } catch (IllegalArgumentException e) {
                    return "Erreur : " + e.getMessage();
                }
            };
        }

        @Bean
        @Description("Crée un nouveau compte bancaire pour le titulaire spécifié.")
        public Function<CreateRequest, String> createAccount(BankAccountService bankAccountService) {
            return req -> {
                String iban = generateIban();
                BankAccount account = new BankAccount(null, req.accountHolder(), iban, 0.0);
                BankAccount saved = bankAccountService.save(account);
                return String.format("Compte créé avec succès pour %s. ID du compte : %d.", saved.getAccountHolder(), saved.getId());
            };
        }

        @Bean
        @Description("Supprime un compte bancaire existant en utilisant son ID.")
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
