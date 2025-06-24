package com.hollybam.hollybam.controller.adminController;

import com.hollybam.hollybam.dto.EventDetailDto;
import com.hollybam.hollybam.dto.EventDto;
import com.hollybam.hollybam.services.IF_EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/admin/event")
public class AdminEventController {

    @Autowired
    private IF_EventService eventService;

    // 이미지 저장 경로 (예: /resources/static/upload/event/)
    private final String uploadDir = "src/main/resources/static/upload/event/";

    @GetMapping("/create")
    public String showEventForm(Model model) {
        model.addAttribute("event", new EventDto());
        return "admin/event/eventCreate";
    }

    @PostMapping("/create")
    public String createEvent(@ModelAttribute EventDto eventDto,
                              @RequestParam("thumbnailFile") MultipartFile thumbnailFile,
                              @RequestParam("imageFile") MultipartFile imageFile,
                              @RequestParam(value = "eventDetailActionUrl", required = false) String actionUrl,
                              RedirectAttributes redirectAttributes) {
        try {
            // ✅ 상태 계산 (before / ing / end)
            LocalDate today = LocalDate.now();
            LocalDate start = eventDto.getEventStart();
            LocalDate end = eventDto.getEventEnd(); // null일 수 있음

            String status;
            if (today.isBefore(start)) {
                status = "before";
            } else if (end == null || !today.isAfter(end)) {
                status = "ing";
            } else {
                status = "end";
            }
            eventDto.setEventStatus(status);

            // ✅ 썸네일 업로드
            String thumbnailFileName = UUID.randomUUID() + "_" + thumbnailFile.getOriginalFilename();
            Path thumbnailPath = Paths.get(uploadDir + thumbnailFileName);
            Files.createDirectories(thumbnailPath.getParent());
            Files.write(thumbnailPath, thumbnailFile.getBytes());
            String thumbnailUrl = "/upload/event/" + thumbnailFileName;
            eventDto.setThumbnailUrl(thumbnailUrl);

            // ✅ 이벤트 저장
            int eventCode = eventService.insertEvent(eventDto);

            // ✅ 상세 이미지 업로드
            String contentFileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            Path contentPath = Paths.get(uploadDir + contentFileName);
            Files.write(contentPath, imageFile.getBytes());
            String imageUrl = "/upload/event/" + contentFileName;

            // ✅ 상세 등록
            EventDetailDto detailDto = new EventDetailDto();
            detailDto.setEventCode(eventCode);
            detailDto.setEventDetailContentImg(imageUrl);
            detailDto.setEventDetailActionUrl(actionUrl);
            eventService.insertEventDetail(detailDto);

            redirectAttributes.addFlashAttribute("success", "이벤트가 등록되었습니다.");
            return "redirect:/admin/event/create";

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "이미지 업로드 실패");
            return "redirect:/admin/event/create";
        }
    }


}

