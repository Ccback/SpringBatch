package com.founder.batch.fcr;

import com.founder.batch.bean.PersonFCRParameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

@Service
public class PersonFCRServiceImpl implements PersonFCRService {
    private static Log log = LogFactory.getLog(PersonFCRServiceImpl.class);
    @Override
    public boolean callPerson(PersonFCRParameter fcr) {
        log.info("fcr操作-------------------------------------------------------------"+fcr);
        return true;
    }
}
