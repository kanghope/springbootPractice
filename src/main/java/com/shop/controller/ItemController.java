package com.shop.controller;

import com.shop.dto.ItemSearchDto;
import com.shop.entity.Item;
import org.apache.ibatis.annotations.Param;
//import org.hibernate.query.Page;
import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.springframework.boot.Banner;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.ui.Model;
import com.shop.dto.ItemFormDto;

import com.shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

//import java.awt.print.Pageable;
import java.util.List;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/item")// 클래스 레벨 매핑: /api/admin/item으로 시작
public class ItemController {

    private final ItemService itemService;

    // -------------------------------------------------------------------------
    //    // 1. GET /admin/item/new (상품등록 폼 요청 - REST 환경)
    //    // -------------------------------------------------------------------------
    @GetMapping(value = "/new")
    public ResponseEntity<String> itemForm() {

        return ResponseEntity.ok("상품등록 폼 준비됨.");
    }
    // -------------------------------------------------------------------------
    //    // 1. POST /api/admin/items (상품 등록 처리)
    //    // -------------------------------------------------------------------------
    @PostMapping(value = "/new")
    public ResponseEntity<?> itemNew(@Valid @RequestPart("itemFormDto") ItemFormDto itemFormDto, BindingResult bindingResult// 💡 itemFormDto의 검사 결과가 여기에 담깁니다.//
            , @RequestPart(value = "itemImgFile", required = false) List<MultipartFile> itemImgFileList)
    {
        // 1. 유효성 검사 실패 (400 Bad Request)
        if(bindingResult.hasErrors())
        {
            /*List<String> errorMessages = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());
            return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);*/
            // 💡 수정된 부분: List<String> 대신 Map<String, String> 반환
            Map<String, String> errorMap = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            error -> error.getField(),// 키: 필드 이름 (예: itemNm, price)
                            error -> error.getDefaultMessage() // 값: 에러 메시지
                    ));
            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        }
        // 2. 첫 번째 이미지 파일 필수 검사 (등록 시에만)
        // 파일이 없거나 비어있는 경우 && ID(수정여부)가 null인 경우
        boolean isImgRequired = (itemImgFileList == null || itemImgFileList.get(0).isEmpty()) && itemFormDto.getId() == null;
        if(isImgRequired)
        {
            //model.addAttribute("errorMessage","첫번째 상품 이미지는 필수 입력 값 입니다.");
            //return "item/itemForm";

            return new ResponseEntity<>("첫번째 상품 이미지는 필수 입력 값 입니다.", HttpStatus.BAD_REQUEST);
        }

        try {
            // 3. 상품 등록 서비스 호출
            // Spring Data save() 결과로 ID가 채워진 DTO를 반환받을 수 있습니다.
            Long saveItemId = itemService.saveItem(itemFormDto, itemImgFileList);

            // 4. 등록 성공 (201 Created)
            return new ResponseEntity<>("상품등록성공, ID:" + saveItemId, HttpStatus.CREATED);
        }
        catch (Exception ex)
        {
//            model.addAttribute("errorMessage","상품 등록 중 에러가 발생하였습니다.");
//            return "item/itemForm";
            // 5. 서버 오류 (500 Internal Server Error)
            return new ResponseEntity<>("상품 등록 중 에러가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //return  "redirect:/";

    }
    // -------------------------------------------------------------------------
    //    // 1. GET /admin/item/new (상품정보조회 ID로  - REST 환경)
    //    // -------------------------------------------------------------------------
    @GetMapping(value = "/{itemId}")
    public ResponseEntity<?> itemDtl(@PathVariable("itemId") Long itemId){
        try{
            // 상품 ID로 DTO 조회
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
            // 200 OK와 함께 DTO 반환
            return ResponseEntity.ok(itemFormDto);

        }
        catch(NoSuchElementException e)
        {
            // 404 Not Found
            // GlobalExceptionHandler에서 NoSuchElementException을 처리하도록 설정할 수도 있습니다.
            //throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다.", e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다.", e);
        }
        catch(Exception e)
        {
            // 기타 서버 오류 (500)
            return new ResponseEntity<>("상품 정보 조회 중 오류가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    // -------------------------------------------------------------------------
    // 3. PUT /api/admin/items/{itemId} (상품 수정 처리)
    // NOTE: 상품 수정을 PUT으로 변경했습니다. 기존 POST /admin/item/{itemId}
    // -------------------------------------------------------------------------
    @PutMapping(value = "/{itemId}")
    public ResponseEntity<?> itemUpdate(@PathVariable("itemId") Long itemId, @RequestPart("itemFormDto") ItemFormDto itemFormDto,
                                        BindingResult bindingResult, @RequestPart(value = "itemImgFile", required = false) List<MultipartFile> itemImgFileList)
    {
        // PathVariable의 ID와 DTO의 ID 일치 확인 (필요시)
        if (itemFormDto.getId() == null || !itemFormDto.getId().equals(itemId)) {
            return new ResponseEntity<>("요청 경로와 상품 ID가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
        // 1. 유효성 검사 실패 (400 Bad Request)
        if(bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());
            return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
        }

        boolean isImgRequired = (itemImgFileList == null || itemImgFileList.get(0).isEmpty()) && itemFormDto.getId() == null;
        if(isImgRequired)
        {
            //model.addAttribute("errorMessage","첫번째 상품 이미지는 필수 입력 값 입니다.");
            //return "item/itemForm";

            return new ResponseEntity<>("첫번째 상품 이미지는 필수 입력 값 입니다.", HttpStatus.BAD_REQUEST);
        }

        try
        {
            Long updatedItemId = itemService.updateItem(itemFormDto, itemImgFileList);

            // 3. 수정 성공 (200 OK)
            return new ResponseEntity<>("상품 수정 성공, ID: " + updatedItemId, HttpStatus.OK);
        }
        catch(NoSuchElementException e)
        {
            // 404 Not Found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "수정하려는 상품이 존재하지 않습니다.", e);
        }
        catch(Exception e)
        {
            // 500 Internal Server Error
            return new ResponseEntity<>("상품 수정 중 에러가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    // -------------------------------------------------------------------------
    // 4. GET /api/admin/items (상품 관리 목록 및 검색/페이징)
    // -------------------------------------------------------------------------
    // 기존의 /admin/items/{page} 방식 대신, 쿼리 파라미터로 page를 받는 RESTful 방식을 사용합니다.
    @GetMapping(value = {"/items/","/items/{page}"})
    public ResponseEntity<Page<?>> itemManage(ItemSearchDto itemSearchDto, // 쿼리 파라미터로 페이지 번호를 받습니다. page는 0부터 시작합니다.
                                              @RequestParam(value = "page", defaultValue = "0") int page,
                                              @RequestParam(value = "size", defaultValue = "3") int size)
    {
        // 🚨 주의: 기존 Thymeleaf 코드에서는 페이지 번호를 1부터 시작하는 것으로 처리했으나,
        // REST API에서는 클라이언트와 백엔드의 혼동을 줄이기 위해 Spring Data JPA의 0-based index (0부터 시작)를 사용하는 것이 일반적입니다.
        // 현재는 0부터 시작하는 페이지 번호를 받도록 수정했습니다. (page 0 -> PageRequest.of(0, 3))

        Pageable pageable = PageRequest.of(page, size);

        // 검색 조건 및 Pageable로 서비스 호출
        Page<?> items = itemService.getAdminItemPage(itemSearchDto, pageable);

        // Page<Item> 객체를 JSON 응답 본문에 담아 200 OK로 반환
        return ResponseEntity.ok(items);
    }

}
