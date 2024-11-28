package hajiboot;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Component // (1)
@Aspect // (2)
public class DetectSlowExecutionAspect {

	private final Logger log = LoggerFactory.getLogger(DetectSlowExecutionAspect.class);

	@Around("execution (@hajiboot.DetectSlowExecution * *.*(..))") // (3)
	public Object detect(ProceedingJoinPoint pjp) throws Throwable {
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();
		DetectSlowExecution detectSlowExecution = AnnotationUtils.getAnnotation(method, DetectSlowExecution.class); // (4)
		long begin = System.currentTimeMillis();
		Object result = pjp.proceed(); // (5)
		long elapsed = System.currentTimeMillis() - begin;
		if (elapsed >= detectSlowExecution.threshold()) { // (6)
			Object[] args = pjp.getArgs(); // (7)
			log.warn("Detect slow execution elapsed={}ms, method={}, args={}", elapsed, method, Arrays.toString(args));
		}
		return result;
	}

}