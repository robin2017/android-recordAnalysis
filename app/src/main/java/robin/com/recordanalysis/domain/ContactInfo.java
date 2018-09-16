package robin.com.recordanalysis.domain;

public class ContactInfo {
    public String number;
    public String name;
    public String company;


    public ContactInfo() {
    }

    public ContactInfo(String number, String name, String company) {
        this.number = number;
        this.name = name;
        this.company = company;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }
}
