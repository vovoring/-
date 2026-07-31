# 마인크래프트 Forge 1.20.1 모드 (자동 빌드)

- `shutdown-timer` — 우측 상단 서버 종료 카운트다운 HUD (클라이언트)
- `cmd-log` — 플레이어 명령어 사용 기록 (호스트만 설치)

## jar 얻는 법 (GitHub Actions)
1. 이 폴더 내용물 전체(`.github` 포함)를 GitHub 저장소에 업로드
2. Actions 탭 → "Build Forge Mods" 완료 대기 (Forge는 첫 빌드 10~20분)
3. Artifacts에서 `shutdown-timer-forge-jar`, `cmd-log-forge-jar` 다운로드 → 압축 해제

## 설치
1. Forge 1.20.1 (47.x) 설치: https://files.minecraftforge.net
2. jar 2개를 `%appdata%\.minecraft\mods`에 넣기 (Fabric API 불필요!)
3. forge-1.20.1 프로필로 실행

## 명령어
- `/shutdowntimer on | off | settime 24:00 | status`
- `/cmdlog recent 20 | search <닉네임> | where` (호스트/OP 전용)
