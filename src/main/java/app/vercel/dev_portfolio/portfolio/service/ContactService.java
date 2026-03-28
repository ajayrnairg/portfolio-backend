package app.vercel.dev_portfolio.portfolio.service;


import app.vercel.dev_portfolio.portfolio.dto.ContactRequest;

public interface ContactService {
    void processMessage(ContactRequest request, String ip);
}