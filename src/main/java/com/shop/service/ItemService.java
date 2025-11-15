package com.shop.service;

import com.shop.dto.ItemFormDto;
import com.shop.dto.MainItemDto;
import com.shop.entity.Item;
import com.shop.entity.ItemImg;
import com.shop.repository.ItemImgRepository;
import com.shop.repository.ItemRepository;
import com.shop.repository.ItemRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import com.shop.dto.ItemImgDto;
//import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;

import java.util.NoSuchElementException;

import com.shop.dto.ItemSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
//import com.shop.dto.MainItemDto;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    private final ItemImgService itemImgService;

    private final ItemImgRepository itemImgRepository;

    private final ItemRepositoryCustom itemRepositoryCustom;

    public Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception{
        // 🚨 1. 현재 사용자 ID 가져와서 설정
        String currentUsername = SecurityUtil.getCurrentUsername();
        itemFormDto.setCreatedBy(currentUsername);
        itemFormDto.setModifiedBy(currentUsername);
        //상품 등록
        Item item = itemFormDto.createItem();
        itemRepository.save(item);

        //이미지 등록
        for(int i=0;i<itemImgFileList.size();i++){
            ItemImg itemImg = new ItemImg();
            itemImg.setItem(item);

            if(i == 0)
                itemImg.setRepImgYn("Y");
            else
                itemImg.setRepImgYn("N");

            itemImgService.saveItemImg(itemImg, itemImgFileList.get(i));
        }

        return item.getId();
    }

    @Transactional(readOnly = true)
    public ItemFormDto getItemDtl(Long itemId){
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemId);
        List<ItemImgDto> itemImgDtoList = new ArrayList<>();
        for (ItemImg itemImg : itemImgList) {
//            if(itemImg != null)
//            {
            ItemImgDto itemImgDto = ItemImgDto.of(itemImg);
            itemImgDtoList.add(itemImgDto);
//            }
        }

        int Maxcount = 5;//맥스값
        //나머지 빈 슬롯을 빈 ItemImgDto 객체로 채움(핵심 수정)
        int RemainCount = Maxcount - itemImgDtoList.size();

        for(int i = 0; i < RemainCount; i++)
        {
            itemImgDtoList.add(new ItemImgDto());//🚨 새로운 빈 ItemImgDto 객체를 추가
        }



        Item item = itemRepository.findById(itemId);
        //.orElseThrow(EntityNotFoundException::new);
        if (item == null) {
            throw new NoSuchElementException("상품을 찾을 수 없습니다. ID: " + itemId);
        }
        ItemFormDto itemFormDto = ItemFormDto.of(item);
        itemFormDto.setItemImgDtoList(itemImgDtoList);
        return itemFormDto;
    }
    public Long updateItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception {

        // 🚨 1. 현재 사용자 ID 가져와서 설정
        String currentUsername = com.shop.service.SecurityUtil.getCurrentUsername();
        itemFormDto.setModifiedBy(currentUsername); // 수정자만 설정

        // 1. 상품 수정 (Item 엔티티 업데이트)
        Item item = itemRepository.findById(itemFormDto.getId());

        if (item == null) {
            throw new NoSuchElementException("수정하려는 상품을 찾을 수 없습니다. ID: " + itemFormDto.getId());
        }
        item.updateItem(itemFormDto); // Item 자체의 정보(이름, 가격 등) 엔티티에 반영

        // 🚨 1-A. 상품 정보를 DB에 UPDATE 하는 코드 추가 (MyBatis 환경 필수)
        itemRepository.update(item);
        // ItemRepository.xml에 <update id="update" ...> 쿼리가 정상적으로 정의되어 있어야 합니다.

        // 외래 키 (Item ID)
        Long itemId = item.getId();

        // 기존 이미지 ID 리스트 (null 포함 5개)
        List<Long> itemImgIds = itemFormDto.getItemImgIds();

        // itemImgFileList는 프론트에서 5개를 보장한다는 전제로 사용합니다.
        List<MultipartFile> files = itemImgFileList != null ? itemImgFileList : new ArrayList<>();

        // ⭐️ [수정 핵심] 루프는 itemImgIds의 크기(프론트에서 보낸 5개)를 기준으로 돌립니다.
        int loopCount = itemImgIds.size(); // 항상 5여야 함

        for (int i = 0; i < loopCount; i++) {

            // 🚨 [수정] 프론트에서 5개를 보냈다고 가정하고 files.get(i)를 호출합니다.
            // itemImgFile은 실제 파일 또는 빈 Placeholder 파일입니다.
            MultipartFile itemImgFile = null;
            if (files.size() > i) {
                itemImgFile = files.get(i);
            } else {
                // ⚠️ 프론트에서 5개 파일을 안 보낸 치명적인 오류 상황입니다.
                // 이 경우에는 처리를 건너뛰거나 예외를 발생시켜야 합니다.
                continue;
            }

            // 1. ItemImg 객체 생성 및 ID, Item 설정
            ItemImg itemImg = new ItemImg();
            itemImg.setId(itemImgIds.get(i)); // ID가 null이면 신규, 아니면 기존
            itemImg.setItem(item);

            // 2. 대표 이미지 여부 설정
            if(i == 0) {
                itemImg.setRepImgYn("Y");
            } else {
                itemImg.setRepImgYn("N");
            }

            // 3. ItemImgService로 처리 위임
            // ⭐️ [핵심] 파일이 빈 파일(Placeholder)이더라도, ID가 null이면 INSERT를 시도하지 않도록
            // ItemImgService에서 파일의 `.isEmpty()`를 체크합니다. (이전 답변에서 ItemImgService 로직이 보정됨)
            itemImgService.updateItemImg(itemImg, itemImgFile);

            // 🚨 파일이 null인 경우의 continue 로직을 제거했습니다.
            // 대신 files.size() < loopCount인 경우에만 continue를 유지합니다.
        }

        return item.getId();
    }

    // --- MyBatis 방식으로 getAdminItemPage 수정 ---
    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable){

        // 1. 페이지네이션 정보 추출
        long offset = pageable.getOffset();
        int limit = pageable.getPageSize();

        // 2. MyBatis로 목록 조회
        List<Item> content = itemRepositoryCustom.getAdminItemPage(itemSearchDto, offset, limit);

        // 3. MyBatis로 전체 카운트 조회
        long total = itemRepositoryCustom.getAdminItemCount(itemSearchDto);

        // 4. PageImpl 객체로 감싸 반환
        return new PageImpl<>(content, pageable, total);
    }

    // --- MyBatis 방식으로 getMainItemPage 수정 ---
    @Transactional(readOnly = true)
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable){

        // 1. 페이지네이션 정보 추출
        long offset = pageable.getOffset();
        int limit = pageable.getPageSize();

        // 2. MyBatis로 목록 조회
        List<MainItemDto> content = itemRepositoryCustom.getMainItemPage(itemSearchDto, offset, limit);

        // 3. MyBatis로 전체 카운트 조회
        long total = itemRepositoryCustom.getMainItemCount(itemSearchDto);

        // 4. PageImpl 객체로 감싸 반환
        return new PageImpl<>(content, pageable, total);
    }


/*
    public Long updateItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception{
        //상품 수정
        Item item = itemRepository.findById(itemFormDto.getId());
                //.orElseThrow(EntityNotFoundException::new);
        if (item == null) {
            throw new NoSuchElementException("수정하려는 상품을 찾을 수 없습니다. ID: " + itemFormDto.getId());
        }
        item.updateItem(itemFormDto);
        List<Long> itemImgIds = itemFormDto.getItemImgIds();

        //이미지 등록
        for(int i=0;i<itemImgFileList.size();i++){
            itemImgService.updateItemImg(itemImgIds.get(i),
                    itemImgFileList.get(i));
        }

        return item.getId();
    }*/
/*
    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable){
        return itemRepository.getAdminItemPage(itemSearchDto, pageable);
    }

    @Transactional(readOnly = true)
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable){
        return itemRepository.getMainItemPage(itemSearchDto, pageable);
    }
*/

}