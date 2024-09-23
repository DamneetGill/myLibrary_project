package com.mylibrary.repository;

import com.mylibrary.entity.Book;
import com.mylibrary.entity.Card;
import com.mylibrary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {
    Optional<Book> findByUserAndCardId(User user, int id);

    Integer countByUser(User user);

    List<Book> findByUser(User user);

    List <Book> findByCardId(int id);
}
