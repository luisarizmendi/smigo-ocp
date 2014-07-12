package org.smigo.user;

import org.hibernate.validator.constraints.Email;

class ResetFormBean {

    @Email
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
