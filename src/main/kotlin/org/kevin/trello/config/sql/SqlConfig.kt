package org.kevin.trello.config.sql

import org.kevin.trello.account.mapper.typeHandler.UserRoleTypeHandler
import org.kevin.trello.account.mapper.typeHandler.UserStatusTypeHandler
import org.mybatis.spring.SqlSessionFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import javax.sql.DataSource

@Configuration
class SqlConfig {

    @Bean("core-sql-session")
    fun factoryBean(dataSource: DataSource): SqlSessionFactoryBean {
        val sqlSessionFactoryBean = SqlSessionFactoryBean()
        sqlSessionFactoryBean.setDataSource(dataSource)
        sqlSessionFactoryBean.setConfigLocation(ClassPathResource("mybatis-config.xml"))
        sqlSessionFactoryBean.addTypeHandlers(
            UserRoleTypeHandler(), UserStatusTypeHandler()
        )
        return sqlSessionFactoryBean
    }
}