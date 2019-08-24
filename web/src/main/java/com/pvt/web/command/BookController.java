package com.pvt.web.command;

import com.google.gson.Gson;
import com.pvt.dto.BookDto;
import com.pvt.entities.Author;
import com.pvt.entities.Book;
import com.pvt.entities.Formular;
import com.pvt.entities.User;
import com.pvt.services.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by w510 on 019 19.09.16.
 */
@Controller
@RequestMapping("/books")
public class BookController {

    public static final String MAIN = "books/main";

    @Autowired
    private BookService bookService;
    @Autowired
    private UserService userService;
    @Autowired
    private FormularService formularService;
    @Autowired
    private AuthorService authorService;

    @RequestMapping(value = "/page", method = {RequestMethod.GET, RequestMethod.POST})
    public String allBooks(ModelMap map, @RequestParam(defaultValue = "") String message) {

        fillModel(map);

        return MAIN;
    }

    @RequestMapping(value = "/addBook", method = {RequestMethod.GET, RequestMethod.POST})
    public String addBooks() {
               return "add";
    }

    @Transactional
    @RequestMapping(value = "/doAdd", method = {RequestMethod.GET, RequestMethod.POST})
    public String addBooks(HttpServletRequest request) {

            String title = request.getParameter("title");
            String ganr = request.getParameter("ganr");
            String pages= request.getParameter("pages");
            String quantity= request.getParameter("quantity");
            String name= request.getParameter("name");
            String country= request.getParameter("coutry");
            String year= request.getParameter("year");

            Author author = new Author(null, name, Integer.parseInt(year), country, null);
            Book book = new Book(null, title, ganr, Integer.parseInt(pages), author, Integer.parseInt(quantity), null);
            authorService.add(author);
            bookService.add(book);

        return "redirect:/books/page";
    }

    @Transactional
    @RequestMapping(value = "/getBook", method = {RequestMethod.GET, RequestMethod.POST})
    public String getBooks(HttpServletRequest request, ModelMap model) {

        long bookId = Long.parseLong(request.getParameter("bookId"));
        User currentUser = getUser();
        long userId = currentUser.getUserId();
        System.out.println("book id = " + bookId + " user id " + userId);

        Book book = bookService.get(bookId);
        int bookCount = book.getBookCount();

        if (bookCount > 0) {
            List<Formular> formulars = formularService.getByUserId(userId);
            if (formulars.size() == 0) {
                addToFormular(book, bookCount, currentUser);
            } else {
                boolean flag = true;
                for (int i = 0; i < formulars.size(); i++) {
                    if (formulars.get(i).getBooks().contains(book)) {
                        flag = false;
                        model.addAttribute("message", "The book has taken yet");
                        break;
                    }
                }
                if (flag == true) {
                    addToFormular(book, bookCount, currentUser);
                }
            }
        }

        return "redirect:/books/page";
//        return "getBook";
    }

    @Transactional
    void addToFormular(Book book, int bookCount, User currentUser) {
        Formular formular = new Formular(null, currentUser, new ArrayList<>());
        book.getFormulars().add(formular);
        formular.getBooks().add(book);
        bookCount--;
        book.setBookCount(bookCount);
        bookService.update(book);
        formularService.add(formular);
    }

    @Transactional
    @RequestMapping(value = "/getBack", method = {RequestMethod.POST})
    public String getBack(HttpServletRequest request, ModelMap model) {

        long bookId = Long.parseLong(request.getParameter("bookId"));
        long userId = Long.parseLong(request.getParameter("userId"));
        doGetBack(bookId, userId);
//        model.addAttribute("message", "Book got back");
        model.addAttribute("userId", userId);

        return "redirect:/formular/page";
    }

    @Transactional
    void doGetBack(long bookId, long userId) {

        List<Formular> formulars = formularService.getByUserId(userId);
        Book book = (Book) bookService.get(bookId);
        int bookCount = book.getBookCount();
        boolean isBookFindedflag = false;
        Long formularId = null;

        for (Formular formular : formulars) {
            List<Book> books = formular.getBooks();
            if (isBookFindedflag == false) {
                for (Book findingBook : books)
                    if (findingBook.equals(book)) {
                        books.remove(book);
                        book.getFormulars().remove(formular);
                        formularId = formular.getFormularId();
                        bookCount++;
                        book.setBookCount(bookCount);
                        bookService.update(book);
                        isBookFindedflag = true;
                        break;
                    }
            }
        }
        if (isBookFindedflag == true) {
            System.out.println(formularId);
            formularService.deleteId(formularId);
        }
    }

    private User getUser() {
        String userName;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            userName = ((UserDetails) principal).getUsername();
        } else {
            userName = principal.toString();
        }
        User currentUser = (User) userService.getByLogin(userName);
        return currentUser;
    }

    private void fillModel(ModelMap model) {
        populatePageName(model);
        model.addAttribute("book", new BookDto());
        model.addAttribute("books", bookService.getAllDto());
    }


    private void populatePageName(ModelMap model) {
        model.addAttribute("currentPageName", "books");
    }
}
