package com.husky.webmvc.context;

import com.husky.webmvc.context.controller.CommentController;
import com.husky.webmvc.context.controller.UserController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClassPathXmlApplicationContextTest {

    @Test
    void testContextInitialize() {
        String configLocation = "/applicationContext.xml";
        ClassPathXmlApplicationContext app = new ClassPathXmlApplicationContext(configLocation);
        UserController userController = (UserController) app.getBean("userController");
        System.out.println("userController = " + userController);
        System.out.println("userController.userService = " + userController.userService);
        System.out.println("userController.userService.userDAO = " + userController.userService.userDAO);
        System.out.println("//////////////////////////");

        CommentController commentController = (CommentController) app.getBean("commentController");
        System.out.println("commentController = " + commentController);
        System.out.println("commentController.commentService = " + commentController.commentService);
        System.out.println("commentController.commentService.commentDAO = " + commentController.commentService.commentDAO);
        System.out.println("commentController.commentService.userService = " + commentController.commentService.userService);

        // check if we have duplicate bean for `userService`
        int expected = userController.userService.hashCode();
        int actual = commentController.commentService.userService.hashCode();
        Assertions.assertEquals(expected, actual, "duplicate bean occur!!");
        System.out.println("ok!");
    }
}