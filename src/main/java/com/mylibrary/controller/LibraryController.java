package com.mylibrary.controller;

import com.mylibrary.entity.Card;
import com.mylibrary.entity.RegisterDto;
import com.mylibrary.entity.User;
import com.mylibrary.repository.BookRepository;
import com.mylibrary.repository.CardRepository;
import com.mylibrary.repository.UserRepository;
import com.mylibrary.service.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class LibraryController {
    @GetMapping("/")
    public String home() {
        return "Home";
    }

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookService bookService;

    @GetMapping("/login")
    public String getLogin() {
        return "login";
    }

    @GetMapping("/catalog")
    public String getCards(Model model) {
        List<Card> cards = cardRepository.findAll();
        model.addAttribute("cards", cards);
        return "catalog";
    }


    @GetMapping("/personalArea")
    public String showPersonalArea(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        model.addAttribute("book", user.getBooks());
        return "personalArea";
    }

    @PostMapping("/borrow")
    public String borrowBook(@RequestParam int id, Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);

        if (bookRepository.findByUserAndCardId(user, id).isPresent()) {
            model.addAttribute("errorMessage", "You've already borrowed this book! Enjoy your reading!");
            return "catalog";
        }

        if (bookRepository.countByUser(user) >= 3) {
            model.addAttribute("errorMessageCount", "You've reached the limit of 3 books borrowable");
            return "catalog";
        }

        if (!bookService.checkOverDue(user)) {
            model.addAttribute("DueDateNotRespected", "You have some overdue books. Please return them before borrowing any new ones");
            return "catalog";
        }
        if (bookService.checkAvailability(id)) {
            model.addAttribute("BookNotAvailable", "The book you are trying to borrow is not available");
            return "catalog";
        }

        bookService.borrowBook(id);
        return ("redirect:/personalArea");

    }

    @PostMapping("/return")
    public String returnBook(@RequestParam int id) {
        bookService.returnBook(id);
        return ("redirect:/personalArea");
    }

    @GetMapping("/register")
    public String register(Model model) {
        RegisterDto registerDto = new RegisterDto();
        model.addAttribute(registerDto);
        return "register";
    }

    @PostMapping("/register")
    public String register(Model model, @Valid @ModelAttribute RegisterDto registerDto, BindingResult result) {
        User user = userRepository.findByUsername(registerDto.getUsername());
        if (user != null) {
            result.addError(new FieldError("registerDto", "username", "Username is already used, please choose a new one"));
        }
        if (result.hasErrors()) {
            return "register";
        }
        var bCryptEncoder = new BCryptPasswordEncoder();

        User newUser = new User();
        newUser.setUsername(registerDto.getUsername());
        newUser.setPassword(bCryptEncoder.encode(registerDto.getPassword()));
        userRepository.save(newUser);
        return "redirect:/login";
    }


}
