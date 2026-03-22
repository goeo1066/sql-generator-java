package com.github.goeo1066.sqlgenerator.config;

import com.github.goeo1066.sqlgenerator.PostgreSqlJdbcTemplate;
import com.github.goeo1066.sqlgenerator.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.ResolvableType;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Set;

@Slf4j
public class PostgreSqlJdbcTemplateRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
    private final String basePackage;

    public PostgreSqlJdbcTemplateRegistryPostProcessor(String basePackage) {
        this.basePackage = basePackage;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Table.class));

        Set<BeanDefinition> candidates = scanner.findCandidateComponents(basePackage);
        for (BeanDefinition candidate : candidates) {
            try {
                String className = candidate.getBeanClassName();
                Class<?> entityClass = Class.forName(className);
                RootBeanDefinition bd = new RootBeanDefinition(PostgreSqlJdbcTemplateFactoryBean.class);
                bd.getConstructorArgumentValues().addGenericArgumentValue(entityClass);
                bd.setTargetType(ResolvableType.forClassWithGenerics(PostgreSqlJdbcTemplate.class, entityClass));

                if (Util.isBlank(candidate.getBeanClassName())) {
                    continue;
                }

                String beanName = "postgreSqlJdbcTemplate_" + entityClass.getSimpleName();
                registry.registerBeanDefinition(beanName, bd);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
