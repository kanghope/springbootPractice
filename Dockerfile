# 기존 FROM openjdk:17-jdk-slim 대신 아래 이미지 사용 (가장 많이 쓰임)
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# 2. 리액트가 포함된 통합 JAR 파일을 컨테이너 안으로 복사
COPY target/*.jar app.jar

# 3. OCI 인증키 파일 복사 (이전에 해결한 부분)
# 경로가 src/main/resources/keys/oracle_private.pem 인 경우
COPY src/main/resources/wallet /app/resources/wallet

# 4. 앱 실행
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]