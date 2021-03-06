package me.hengdao.plugin;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.hengdao.converter.SqlConverter;
import me.hengdao.util.ReflectionUtils;

import java.sql.Connection;
import java.util.Properties;

/**
 * @Title: ShardPlugin.java
 * @Package org.summercool.util.ibatis.plugin
 * @Description: Ibatis分表插件
 * @author 简道
 * @date 2011-8-12 上午11:15:17
 * @version V1.0
 */
@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class }) })
public class ShardPlugin implements Interceptor {

	private static final Logger log = LoggerFactory.getLogger(ShardPlugin.class);

	private SqlConverter sqlConverter;

	public SqlConverter getSqlConverter() {
		return sqlConverter;
	}

	public void setSqlConverter(SqlConverter sqlConverter) {
		this.sqlConverter = sqlConverter;
	}

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		StatementHandler statementHandler = (StatementHandler) invocation.getTarget();

		String sql = statementHandler.getBoundSql().getSql();
		if (log.isDebugEnabled()) {
			log.debug("Original Sql [" + sql + "]");
		}

		String targetSql = sqlConverter.convert(sql, statementHandler);
		if (log.isDebugEnabled()) {
			log.debug("Converted Sql [" + targetSql + "]");
		}

		if (!sql.equals(targetSql)) {
			ReflectionUtils.setFieldValue(statementHandler.getBoundSql(), "sql", targetSql);
		}

		return invocation.proceed();
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
		//
	}

}
