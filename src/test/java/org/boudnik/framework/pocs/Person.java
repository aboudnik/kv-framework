package org.boudnik.framework.pocs;

import org.boudnik.framework.OBJ;

/**
 * @author Sergey Nuyanzin
 * @since 4/23/2018
 */
public class Person extends OBJ.Implementation<String> {
    public String fname;
    public String lname;

    public Person(String ssn, String fname, String lname) {
        super(ssn);
        this.fname = fname;
        this.lname = lname;
    }

    @Override
    public String toString() {
        return "Person{" +
                "fname='" + fname + '\'' +
                ", lname='" + lname + '\'' +
                '}';
    }
}
