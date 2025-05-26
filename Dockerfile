FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# Gradle Wrapper와 build 파일들 복사
COPY settings.gradle.kts .
COPY gradle.properties .
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .

# 소스 코드 복사
COPY src src

# 실행 권한 부여
RUN chmod +x ./gradlew

# 의존성 다운로드 및 애플리케이션 빌드
RUN ./gradlew build --no-daemon

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행df
CMD ["./gradlew", "run", "--no-daemon"]