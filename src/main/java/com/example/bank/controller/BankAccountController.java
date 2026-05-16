package com.example.bank.controller;

import com.example.bank.model.BankAccount;
import com.example.bank.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/accounts")
public class BankAccountController {

    @Autowired
    private BankAccountService bankAccountService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("accounts", bankAccountService.findAll());
        return "list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("account", new BankAccount());
        model.addAttribute("isNew", true);
        return "form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute BankAccount account) {
        bankAccountService.save(account);
        return "redirect:/accounts";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("account", bankAccountService.findById(id));
        return "view";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("account", bankAccountService.findById(id));
        model.addAttribute("isNew", false);
        return "form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        bankAccountService.deleteById(id);
        return "redirect:/accounts";
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam Long id, @RequestParam Double amount) {
        try {
            bankAccountService.deposit(id, amount);
        } catch (IllegalArgumentException e) {
            // silently redirect on error
        }
        return "redirect:/accounts/" + id;
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam Long id, @RequestParam Double amount) {
        try {
            bankAccountService.withdraw(id, amount);
        } catch (IllegalArgumentException e) {
            // silently redirect on error
        }
        return "redirect:/accounts/" + id;
    }
}
