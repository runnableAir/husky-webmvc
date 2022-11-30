package com.husky.webmvc.context;

import com.husky.webmvc.context.controller.CommentController;
import com.husky.webmvc.context.controller.UserController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class BeanFactoryTest {


    @Test
    void testBeanFactory() {
        BeanFactory beanFactory = getBeanFactory();
        UserController userController = (UserController) beanFactory.getBean("userController");
        System.out.println("userController = " + userController);
        System.out.println("userController.userService = " + userController.userService);
        System.out.println("userController.userService.userDAO = " + userController.userService.userDAO);
        System.out.println("//////////////////////////");
        CommentController commentController = (CommentController) beanFactory.getBean("commentController");
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

    private BeanFactory getBeanFactory() {
        BeanDefinition userDAO = new BeanDefinition("userDAO", "com.husky.webmvc.context.dao.UserDAO");
        BeanDefinition commentDAO = new BeanDefinition("commentDAO", "com.husky.webmvc.context.dao.CommentDAO");
        BeanDefinition userService = new BeanDefinition("userService", "com.husky.webmvc.context.service.UserService");
        BeanDefinition commentService = new BeanDefinition("commentService", "com.husky.webmvc.context.service.CommentService");
        BeanDefinition commentController = new BeanDefinition("commentController", "com.husky.webmvc.context.controller.CommentController");
        BeanDefinition userController = new BeanDefinition("userController", "com.husky.webmvc.context.controller.UserController");

        Map<String, BeanDefinition> userServiceProperties = new HashMap<>();
        Map<String, BeanDefinition> userControllerProperties = new HashMap<>();
        Map<String, BeanDefinition> commentServiceProperties = new HashMap<>();
        Map<String, BeanDefinition> commentControllerProperties = new HashMap<>();

        userServiceProperties.put("userDAO", userDAO);
        commentServiceProperties.put("commentDAO", commentDAO);
        commentServiceProperties.put("userService", userService);
        userControllerProperties.put("userService", userService);
        commentControllerProperties.put("commentService", commentService);

        userService.setPropertyDefinition(userServiceProperties);
        commentService.setPropertyDefinition(commentServiceProperties);
        userController.setPropertyDefinition(userControllerProperties);
        commentController.setPropertyDefinition(commentControllerProperties);

        BeanFactory beanFactory = new BeanFactory();
        for (BeanDefinition definition : Arrays.asList(userDAO, commentDAO, userService, commentService, userController, commentController)) {
            beanFactory.registerBean(definition);
        }
        return beanFactory;
    }

}