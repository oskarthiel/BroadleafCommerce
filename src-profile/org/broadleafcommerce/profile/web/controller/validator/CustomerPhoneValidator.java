/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.broadleafcommerce.profile.web.controller.validator;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.profile.domain.CustomerPhone;
import org.broadleafcommerce.profile.domain.Phone;
import org.broadleafcommerce.profile.service.CustomerPhoneService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component("blCustomerPhoneValidator")
public class CustomerPhoneValidator implements Validator {
    /** Logger for this class and subclasses */
    protected final Log logger = LogFactory.getLog(getClass());

    @Resource
    private final CustomerPhoneService customerPhoneService;

    public CustomerPhoneValidator(){
        this.customerPhoneService = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean supports(Class clazz) {
        return clazz.equals(Phone.class);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        //use regular phone
        CustomerPhone cPhone = (CustomerPhone) obj;

        if (!errors.hasErrors()) {
            //check for duplicate phone number
            List<CustomerPhone> phones = customerPhoneService.readAllCustomerPhonesByCustomerId(cPhone.getCustomerId());

            String phoneNum = cPhone.getPhone().getPhoneNumber();
            String phoneName = cPhone.getPhoneName();

            Long phoneId = cPhone.getPhone().getId();
            Long customerPhoneId = cPhone.getId();

            boolean foundPhoneIdForUpdate = false;
            boolean foundCustomerPhoneIdForUpdate = false;

            for (CustomerPhone existingPhone : phones) {
                //validate that the phoneId passed for an editPhone scenario exists for this user
                if(phoneId != null && !foundPhoneIdForUpdate){
                    if(existingPhone.getPhone().getId().equals(phoneId)){
                        foundPhoneIdForUpdate = true;
                    }
                }

                //validate that the customerPhoneId passed for an editPhone scenario exists for this user
                if(customerPhoneId != null && !foundCustomerPhoneIdForUpdate){
                    if(existingPhone.getId().equals(customerPhoneId)){
                        foundCustomerPhoneIdForUpdate = true;
                    }
                }

                if(existingPhone.getId().equals(cPhone.getId())){
                    continue;
                }

                if(phoneNum.equals(existingPhone.getPhone().getPhoneNumber())){
                    errors.pushNestedPath("phone");
                    errors.rejectValue("phoneNumber", "phoneNumber.duplicate", null);
                    errors.popNestedPath();
                }

                if(phoneName.equalsIgnoreCase(existingPhone.getPhoneName())){
                    errors.rejectValue("phoneName", "phoneName.duplicate", null);
                }
            }

            if(phoneId != null && !foundPhoneIdForUpdate){
                errors.pushNestedPath("phone");
                errors.rejectValue("id", "phone.invalid_id", null);
                errors.popNestedPath();
            }

            if(customerPhoneId != null && !foundCustomerPhoneIdForUpdate){
                errors.rejectValue("id", "phone.invalid_id", null);
            }
        }
    }
}
