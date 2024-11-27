package hajiboot;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Qualifier
public @interface Online {

}