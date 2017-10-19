package com.jaguar.servlet;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Any requests to /client/api/files/* should be changed to /* since all static files will reside under the root webapp folder.
 * Note that if there are any requests to png or js resources, this servlet will be called again.
 */
@WebServlet("/files/*")
public class FileServlet extends HttpServlet {

    private static final Logger fileServletLogger = Logger.getLogger(FileServlet.class.getSimpleName());
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        fileServletLogger.info("Context Path : "+req.getContextPath());
        fileServletLogger.info("Request URI "+req.getRequestURI());
        final String requestURI = req.getRequestURI();
        final String[] pathParts = requestURI.split("/");
        if(pathParts.length == 0) {
            fileServletLogger.error("The request URI does not contain any paths!");
            resp.sendError(HttpStatus.NOT_FOUND.value());
        }
        if(!(pathParts.length >= 2 )) {
            fileServletLogger.error("Request URI is not in the correct format");
            resp.sendError(HttpStatus.BAD_REQUEST.value());
        }
        req.getRequestDispatcher("/" +pathParts[pathParts.length - 1]).forward(req,resp);
    }
}
