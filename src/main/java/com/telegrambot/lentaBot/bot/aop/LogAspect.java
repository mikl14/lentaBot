package com.telegrambot.lentaBot.bot.aop;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegrambot.lentaBot.bot.requests.Status;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import com.telegrambot.lentaBot.bot.annotations.Logging;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Log4j2
@Component
public class LogAspect {

    private static final Logger logger = LogManager.getLogger(LogAspect.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Around(value = "@annotation(logging)")
    public Object logMethodExit(ProceedingJoinPoint joinPoint, Logging logging) throws Throwable {
        Map<String, Object> logMap = new HashMap<>();

        Object result = joinPoint.proceed();

        logMap.put("timestamp", LocalDateTime.now().format(dateTimeFormatter));
        logMap.put("level", logging.level());
        logMap.put("method", joinPoint.getSignature().toShortString());
        if (logging.entering()) {

            if (!logging.value().isEmpty()) {
                logMap.put("value", logging.value());
                logger.log(Level.toLevel(logging.level()), ">> " + logging.value());
            } else {
                logger.log(Level.toLevel(logging.level()), ">> " + joinPoint.getSignature().getName());
            }

        }
        if (logging.argsData()) {
            logMap.put("args", joinPoint.getArgs().toString());
        }

        if (logging.exiting()) {

            if (result != null) {
                if (result.toString().equals(Status.FAIL.toString())) {
                    logMap.put("return", result.toString());
                    logMap.put("level", Level.ERROR.toString());
                    logger.log(Level.ERROR, result.toString());
                } else {
                    logMap.put("return", result.toString());
                    logger.log(Level.valueOf(logging.level()), result.toString());
                }

            } else {
                logMap.put("level", Level.ERROR.toString());
                logMap.put("return", NullPointerException.class.toString());
                logger.log(Level.ERROR, NullPointerException.class.toString());
            }
        }
        if (logging.exiting() || logging.entering()) {
            String currentDateString = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String logFileName = "logs/log-" + currentDateString + ".json";

            try {
                String logJson = objectMapper.writeValueAsString(logMap);
                FileWriter fileWriter = new FileWriter(logFileName, true);
                fileWriter.write(logJson + "\n");
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
