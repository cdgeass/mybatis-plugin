package io.github.cdgeass.formatter;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.util.JdbcUtils;
import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author cdgeass
 * @since 2020-04-21
 */
public class Formatter {

    private static final Logger log = Logger.getInstance(Formatter.class);

    private static final Pattern SET_PARAM_REGEX = Pattern.compile("(?<=[=(,]\\s)\\?|\\?(?:\\s+[=><])");
    private static final Pattern GET_PARAM_TYPE_PATTERN = Pattern.compile("(\\b.*)\\((\\S+)\\)");

    protected static String format(String preparing, String[] parameters) {
        if (StringUtils.isBlank(preparing) || ArrayUtils.isEmpty(parameters)) {
            return "";
        }


        return SQLUtils.format(preparing, JdbcUtils.MYSQL, parameters(parameters));
    }

    private static List<Object> parameters(String[] parametersWithType) {
        List<Object> parameters = Lists.newArrayList();

        for (String parameterWithType : parametersWithType) {
            var matcher = GET_PARAM_TYPE_PATTERN.matcher(parameterWithType);
            if (matcher.find()) {
                var parameter = matcher.group(1);
                var parameterType = matcher.group(2);

                switch (parameterType) {
                    case "Integer":
                        parameters.add(Integer.valueOf(parameter));
                        break;
                    // TODO: add other type
                    default:
                        parameters.add(parameter);
                        break;
                }
            }
        }

        return parameters;
    }
}
