package com.almaakcorp.entreprise.audit;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditingAspect {

    private final AuditService auditService;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(com.almaakcorp.entreprise.audit.Audited)")
    public Object aroundAudited(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Audited ann = method.getAnnotation(Audited.class);
        String action = ann.action();
        String entity = ann.entity();

        Object result = null;
        boolean success = false;
        String error = null;
        String entityId = null;

        try {
            result = pjp.proceed();
            success = true;
            entityId = resolveEntityId(ann.entityIdSpEL(), signature.getParameterNames(), pjp.getArgs(), result);
            return result;
        } catch (Throwable ex) {
            error = ex.getMessage();
            entityId = resolveEntityId(ann.entityIdSpEL(), signature.getParameterNames(), pjp.getArgs(), null);
            throw ex;
        } finally {
            Object details = success ? result : null; // avoid logging inputs containing secrets; can adjust
            auditService.log(action, entity, entityId, details, success, error);
        }
    }

    private String resolveEntityId(String spel, String[] paramNames, Object[] args, Object result) {
        if (spel == null || spel.isBlank()) return null;
        try {
            EvaluationContext ctx = new StandardEvaluationContext();
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length && i < args.length; i++) {
                    ctx.setVariable(paramNames[i], args[i]);
                }
            }
            ctx.setVariable("result", result);
            Expression exp = parser.parseExpression(spel);
            Object val = exp.getValue(ctx);
            return val != null ? String.valueOf(val) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
