package com.ndmc.ndmc_record.utils;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.LongType;


import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Properties;

public class StringPrefixedSequenceIdGenerator extends SequenceStyleGenerator {

//    public static final String VALUE_PREFIX_PARAMETER = "valuePrefix";
//    public static final String VALUE_PREFIX_DEFAULT = "";
//    private String valuePrefix;
//
//    public static final String NUMBER_FORMAT_PARAMETER = "numberFormat";
//    public static final String NUMBER_FORMAT_DEFAULT = "%d";
//    private String numberFormat;
//
//    @Override
//    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
//        //return valuePrefix + String.format(numberFormat, super.generate(session, object));
//        return valuePrefix + super.generate(session, object);
//    }
//
//    @Override
//    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
//        super.configure(LongType.INSTANCE, params, serviceRegistry);
//        valuePrefix = ConfigurationHelper.getString(VALUE_PREFIX_PARAMETER,
//                params, VALUE_PREFIX_DEFAULT);
//        numberFormat = ConfigurationHelper.getString(NUMBER_FORMAT_PARAMETER,
//                params, NUMBER_FORMAT_DEFAULT);
//    }

}
