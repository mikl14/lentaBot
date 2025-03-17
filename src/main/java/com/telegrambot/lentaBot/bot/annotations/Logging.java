package com.telegrambot.lentaBot.bot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Logging {
    String value() default ""; // Текст с описанием работы метода

    boolean entering() default false; // Флаг для логирования входа в метод

    boolean argsData() default false;

    boolean returnData() default false;

    boolean exiting() default false; // Флаг для логирования выхода из метода

    String level() default "INFO"; // Уровень логирования по умолчанию
}
