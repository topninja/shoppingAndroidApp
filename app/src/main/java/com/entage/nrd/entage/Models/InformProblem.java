package com.entage.nrd.entage.Models;

public class InformProblem {

    String user_id;
    String email;
    String phone_number;

    String type_problem;
    String title_problem;
    String text_problem;

    String entered_email;

    String date;

    String problem_id;

    boolean contact_me_on_app;

    public InformProblem() {
    }

    public InformProblem(String user_id, String email, String phone_number, String type_problem, String title_problem, String text_problem, String entered_email, String date, String problem_id, boolean contact_me_on_app) {
        this.user_id = user_id;
        this.email = email;
        this.phone_number = phone_number;
        this.type_problem = type_problem;
        this.title_problem = title_problem;
        this.text_problem = text_problem;
        this.entered_email = entered_email;
        this.date = date;
        this.problem_id = problem_id;
        this.contact_me_on_app = contact_me_on_app;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getType_problem() {
        return type_problem;
    }

    public void setType_problem(String type_problem) {
        this.type_problem = type_problem;
    }

    public String getTitle_problem() {
        return title_problem;
    }

    public void setTitle_problem(String title_problem) {
        this.title_problem = title_problem;
    }

    public String getText_problem() {
        return text_problem;
    }

    public void setText_problem(String text_problem) {
        this.text_problem = text_problem;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEntered_email() {
        return entered_email;
    }

    public void setEntered_email(String entered_email) {
        this.entered_email = entered_email;
    }

    public String getProblem_id() {
        return problem_id;
    }

    public void setProblem_id(String problem_id) {
        this.problem_id = problem_id;
    }

    @Override
    public String toString() {
        return "InformProblem{" +
                "user_id='" + user_id + '\'' +
                ", email='" + email + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", type_problem='" + type_problem + '\'' +
                ", title_problem='" + title_problem + '\'' +
                ", text_problem='" + text_problem + '\'' +
                ", entered_email='" + entered_email + '\'' +
                ", date='" + date + '\'' +
                ", problem_id='" + problem_id + '\'' +
                '}';
    }
}
