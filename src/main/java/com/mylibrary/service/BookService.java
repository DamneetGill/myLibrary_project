package com.mylibrary.service;

import com.mylibrary.entity.Book;
import com.mylibrary.entity.Card;
import com.mylibrary.entity.User;
import com.mylibrary.repository.BookRepository;
import com.mylibrary.repository.CardRepository;
import com.mylibrary.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class BookService {
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private UserRepository userRepository;

    public void borrowBook(int cardId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new RuntimeException("Card not found"));

        if (bookRepository.findByUserAndCardId(user, cardId).isPresent()) {
            throw new RuntimeException("You've already borrowed this book!");
        }

        Book book = new Book();
        book.setCard(card);
        book.setUser(user);
        book.setTitle(card.getTitle());
        book.setBorrowDate(LocalDate.now());
        book.setDueDate(book.getBorrowDate().plusDays(30));

        bookRepository.save(book);
    }

    public boolean checkOverDue(User user) {
        return bookRepository
                .findByUser(user)
                .stream()
                .filter
                        (book -> book.getDueDate().isBefore(LocalDate.now()))
                .count() == 0;

    }

    public boolean checkAvailability(int id) {
        System.out.println(id);
        Card card = cardRepository.findById(id).orElseThrow(() -> new RuntimeException("Card not found"));
        System.out.println(card.getAvailability());
        long conta = bookRepository
                .findByCardId(id)
                .stream()
                .count();
        System.out.println(conta);
        return conta == card.getAvailability();
    }

    public void returnBook(int id) {
        bookRepository.deleteById(id);
    }
}
