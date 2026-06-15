package com.siblo.rent.controller;

import com.siblo.rent.dto.CourtDTO;
import com.siblo.rent.dto.SportDTO;
import com.siblo.rent.service.CourtService;
import com.siblo.rent.service.SportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
public class PageController {

    private final SportService sportService;
    private final CourtService courtService;

    public PageController(SportService sportService, CourtService courtService) {
        this.sportService = sportService;
        this.courtService = courtService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        List<SportDTO> sports = sportService.getAllSports();
        List<CourtDTO> courts = courtService.getActiveCourts(null);
        long availableCount = courtService.getAvailableCourtsCount();
        model.addAttribute("sports", sports);
        model.addAttribute("courts", courts);
        model.addAttribute("availableCount", availableCount);
        model.addAttribute("activePage", "home");
        return "home";
    }

    @GetMapping("/booking")
    public String booking(@RequestParam(required = false) Long courtId, Model model) {
        if (courtId != null) {
            try {
                model.addAttribute("court", courtService.getCourtById(courtId));
            } catch (RuntimeException e) {
                addFirstCourt(model);
            }
        } else {
            addFirstCourt(model);
        }
        model.addAttribute("activePage", "booking");
        return "booking";
    }

    private void addFirstCourt(Model model) {
        List<CourtDTO> courts = courtService.getActiveCourts(null);
        if (!courts.isEmpty()) model.addAttribute("court", courts.get(0));
    }

    @GetMapping("/my-bookings")
    public String myBookings(Model model) {
        model.addAttribute("activePage", "my-bookings");
        return "my-bookings";
    }

    @GetMapping("/manage-admin")
    public String adminManagement(Model model) {
        model.addAttribute("activePage", "admin");
        return "manage-admin";
    }

    @GetMapping("/login")
    public String login() { return "login"; }
}
