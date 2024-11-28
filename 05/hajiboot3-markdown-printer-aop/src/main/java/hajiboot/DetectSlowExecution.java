package hajiboot;

import java.lang.annotation.*;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DetectSlowExecution {

	long threshold() default 500; // (1)

}
