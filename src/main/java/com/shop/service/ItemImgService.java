package com.shop.service;

import com.shop.entity.ItemImg;
import com.shop.repository.ItemImgRepository; // 💡 ItemImgRepository 대신 ItemImgMapper 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;
import java.util.NoSuchElementException;
import java.util.Optional; // Optional 임포트 (findById 반환값 처리를 위해)

@Service
@RequiredArgsConstructor
@Transactional// MyBatis 환경에서도 Spring의 트랜잭션 관리는 그대로 사용 가능
public class ItemImgService {

    @Value("${itemImgLocation}")
    private String itemImgLocation;// "item" (버킷명)

    @Value("${itemImgBaseUrl}")
    private String itemImgBaseUrl; // "https://.../o/"

    // 💡 변경: ItemImgRepository 대신 ItemImgMapper 주입
    private final ItemImgRepository itemImgRepository;
    private final FileService fileService;

    public void saveItemImg(ItemImg itemImg, MultipartFile itemImgFile) throws Exception{

        // 🚨 1단계: 파일이 비어있는지 확인하고, 비어있다면 메서드를 즉시 종료합니다.
        if (itemImgFile == null || itemImgFile.isEmpty()) {
            // 빈 파일이 넘어왔으므로 DB 저장을 건너뜁니다.
            return;
        }
        String oriImgName = itemImgFile.getOriginalFilename();
        String imgName = "";
        String imgUrl = "";

        //파일 업로드
        //기존 내부 하드디스크 저장용 소스 일단 주석
        /*
        if(!StringUtils.isEmpty(oriImgName)){
            imgName = fileService.uploadFile(itemImgLocation, oriImgName, itemImgFile.getBytes());
            imgUrl = "/images/item/" + imgName;
        }*/
        // 파일 업로드 (내부적으로 OCI 서버로 전송됨)
        if(!StringUtils.isEmpty(oriImgName))
        {
            imgName = fileService.uploadFile(itemImgLocation, oriImgName, itemImgFile.getBytes());
            // 💡 클라우드 URL 생성: https://.../o/ + uuid.jpg
            imgUrl = itemImgBaseUrl + imgName;
        }

        //상품 이미지 정보 저장
        itemImg.updateItemImg(oriImgName, imgName, imgUrl);

        // 💡 변경: itemImgRepository.save() -> itemImgMapper.save()
        itemImgRepository.save(itemImg);
    }
/*
    public void updateItemImg(Long itemImgId, MultipartFile itemImgFile) throws Exception{
        if(!itemImgFile.isEmpty()){
            // 💡 변경: findById의 반환 타입 처리 방식 수정
            // MyBatis의 findById는 Optional이 아닌 ItemImg 객체를 바로 반환하도록 XML을 구현했다고 가정합니다.
            ItemImg savedItemImg = itemImgRepository.findById(itemImgId);

            if (savedItemImg == null) {
                // 객체를 찾지 못하면 예외 발생
                throw new IllegalStateException("ItemImg not found with ID: " + itemImgId);
            }

            //기존 이미지 파일 삭제
            if(!StringUtils.isEmpty(savedItemImg.getImgName())) {
                fileService.deleteFile(itemImgLocation+"/"+
                        savedItemImg.getImgName());
            }

            String oriImgName = itemImgFile.getOriginalFilename();
            String imgName = fileService.uploadFile(itemImgLocation, oriImgName, itemImgFile.getBytes());
            String imgUrl = "/images/item/" + imgName;

            // ItemImg 엔티티는 JPA 유무와 상관없이 동일하게 동작
            savedItemImg.updateItemImg(oriImgName, imgName, imgUrl);

            // 💡 변경: update 쿼리가 필요함 (엔티티 상태 변경만으로는 DB에 반영되지 않음)
            itemImgRepository.update(savedItemImg); // <--- XML에 update 쿼리를 추가해야 함
        }
    }
*/
    // 🚨 주의: 메서드 시그니처를 ItemImg 엔티티를 받도록 변경해야 합니다.
    public void updateItemImg(ItemImg itemImg, MultipartFile itemImgFile) throws Exception{

        // 1. 기존 이미지(UPDATE)인지 새로운 이미지(INSERT)인지 확인
        if (itemImg.getId() != null && itemImg.getId() > 0) {
            // ----------------------------------------------------
            // 💡 UPDATE: ItemImg ID가 존재하면 기존 이미지 수정 또는 유지
            // ----------------------------------------------------

            // **[수정] 파일이 비어 있으면 기존 이미지를 유지하고 즉시 종료합니다.**
            if (itemImgFile.isEmpty()) {
                return; // ⭐️ 기존 이미지 정보를 유지하고, DB 업데이트 로직은 건너뜁니다.
            }

            // 파일이 비어있지 않다면 (새로운 파일이 선택되었다면) 나머지 로직 수행

            // DB에서 기존 정보 조회 (기존 파일 이름 확인을 위해)
            ItemImg savedItemImg = itemImgRepository.findById(itemImg.getId());

            if (savedItemImg == null) {
                throw new IllegalStateException("ItemImg not found with ID: " + itemImg.getId());
            }

            // 기존 파일 삭제
            if (!StringUtils.isEmpty(savedItemImg.getImgName())) {
                fileService.deleteFile(itemImgLocation + "/" + savedItemImg.getImgName());
            }

            // 새 파일 업로드 및 URL 생성
            String oriImgName = itemImgFile.getOriginalFilename();
            String imgName = fileService.uploadFile(itemImgLocation, oriImgName, itemImgFile.getBytes());
            //String imgUrl = "/images/item/" + imgName;
            String imgUrl = itemImgBaseUrl + imgName;

            // 엔티티 업데이트 및 DB 반영
            savedItemImg.updateItemImg(oriImgName, imgName, imgUrl);
            itemImgRepository.update(savedItemImg); // DB UPDATE 실행

        } else {
            // ----------------------------------------------------
            // 💡 INSERT: ItemImg ID가 없으면 새로운 이미지 등록
            // ----------------------------------------------------

            // **[수정] 파일이 비어 있으면 INSERT를 시도할 필요가 없으므로 종료합니다.**
            if (itemImgFile.isEmpty()) {
                return; // ⭐️ 파일이 없으면 새로운 이미지를 등록할 수 없습니다.
            }

            // 파일 업로드 및 URL 생성
            String oriImgName = itemImgFile.getOriginalFilename();
            String imgName = fileService.uploadFile(itemImgLocation, oriImgName, itemImgFile.getBytes());
            //String imgUrl = "/images/item/" + imgName;
            String imgUrl = itemImgBaseUrl + imgName;

            // ItemImg 엔티티에 파일 정보 설정
            itemImg.updateItemImg(oriImgName, imgName, imgUrl);

            // INSERT 실행
            itemImgRepository.save(itemImg);
        }
    }
}