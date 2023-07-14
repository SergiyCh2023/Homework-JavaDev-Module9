import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;




@WebServlet("/time")
public class ThymeleafTestController extends HttpServlet {
    private TemplateEngine engine;
    private String currentTimeUTC;

    private Cookie cookie;

    @Override
    public void init() throws ServletException {
        engine = new TemplateEngine();



        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix(getServletContext().getRealPath("templates/"));
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/html; charset=utf-8");
        String utc = req.getParameter("timezone");


        if (utc == null) {

            if (cookie==null) {
                currentTimeUTC = LocalDateTime.now().format(DateTimeFormatter.ofPattern(
                        " yyyy-MM-dd ''  HH:mm:ss  "));
                        utc = "UTC";
            }  else {
                Cookie[] cookies = req.getCookies();

                if (cookies!=null) {
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().equals("lastTimezone")) utc = cookie.getValue();
                    }
                }
                if (utc!=null) {
                    currentTimeUTC = LocalDateTime.now(ZoneId.of(utc.replace(" ", ""))).format(DateTimeFormatter.ofPattern(
                            " yyyy-MM-dd  ''  HH:mm:ss  "));
                            utc = utc;
                }
            }


        } else {
            utc = utc.toUpperCase();

            if (!utc.contains("-") && !utc.contains("Z")) {
                if (utc.contains("UTC")) utc = (new StringBuilder(utc.replace(" ", ""))).insert(3, "+").toString();
                else utc = "+" + utc.replace(" ", "");
            }

            resp.addCookie(cookie = new Cookie("lastTimezone", utc));

            currentTimeUTC = LocalDateTime.now(ZoneId.of(utc.replace(" ", ""))).format(DateTimeFormatter.ofPattern(
                    " yyyy-MM-dd '' HH:mm:ss  "));
        }


            Context simpleContext = new Context(
                    req.getLocale(),
                    Map.of("currentTime", currentTimeUTC, "UTC", utc)
            );


        // 2022-01-05 12:05:01 UTC+2
            engine.process("index", simpleContext, resp.getWriter());
            resp.getWriter().close();
        }
    }
