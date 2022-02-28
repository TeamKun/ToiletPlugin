package net.kunmc.lab.toiletplugin.game.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Config
{
    String helpMessage();

    double min() default -1;

    double max() default -1;

    String[] enums() default {};

    boolean ranged() default false;
}
