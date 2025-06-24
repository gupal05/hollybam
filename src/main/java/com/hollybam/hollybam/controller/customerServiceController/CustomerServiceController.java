package com.hollybam.hollybam.controller.customerServiceController;

import com.hollybam.hollybam.dto.EventDetailDto;
import com.hollybam.hollybam.dto.EventDto;
import com.hollybam.hollybam.dto.InquiryDto;
import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.services.EventService;
import com.hollybam.hollybam.services.InquiryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/customer-service")
public class CustomerServiceController {
    @Autowired
    EventService eventService;
    @Autowired
    InquiryService inquiryService;

    @GetMapping
    public String customerService(Model model, HttpSession session){
        // 이벤트 리스트 get
        List<EventDto> eventList = eventService.selectVisibleEvents();
        model.addAttribute("eventList", eventList);

        // 1:1 문의 내역
        MemberDto memberDto = (MemberDto) session.getAttribute("member");
        List<InquiryDto> inquiryList = inquiryService.selectInquiryList(memberDto.getMemberCode());
        model.addAttribute("inquiryList", inquiryList);
        return "/cs";
    }

    @GetMapping("/event/detail/{code}")
    public String eventDetail(@PathVariable("code") int eventCode, Model model) {
        EventDto event = eventService.selectEventByCode(eventCode);
        EventDetailDto detail = eventService.selectEventDetailByEventCode(eventCode);
        model.addAttribute("event", event);
        model.addAttribute("detail", detail);
        return "/eventDetail";  // 템플릿 이름
    }

    @PostMapping("/inquiry")
    @ResponseBody
    public ResponseEntity<Boolean> insertInquiry(@ModelAttribute InquiryDto inquiryDto, HttpSession session) {
        MemberDto memberDto = (MemberDto) session.getAttribute("member");
        inquiryDto.setInquiryCode(memberDto.getMemberCode());
        return ResponseEntity.ok(inquiryService.insertInquiry(inquiryDto) > 0);
    }

}
