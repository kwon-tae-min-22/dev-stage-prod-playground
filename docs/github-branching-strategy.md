# GitHub 브랜칭 전략

## 브랜치 역할
- `main`: 스테이징 용도. 테스트 서버에 배포되는 기준 브랜치이며, 운영 배포 전에 변경사항을 모은다.
- `release`: 운영(프로덕션) 브랜치. 운영 배포는 `release` 기준으로 진행한다.
- `feature/*`: 신규 기능 개발용 작업 브랜치.
- `fix/*`: 발견된 문제나 잘못 구현된 부분을 수정하는 작업 브랜치(스테이징 단계에서 발견된 버그 등). 항상 `main`에서 분기해 `main`으로 PR을 올리고, 배포 시점에 `main`을 `release`로 올린다.
- `hotfix/*`: 운영 중 긴급 이슈를 즉시 수정하는 브랜치. `release`에서 분기한다.
  - 대응 이후 main도 최신 상태를 가져야 하므로, 배포 후 `release`를 `main`으로 역병합(또는 `cherry-pick`)해 코드 동기화를 유지. 
- `chore/*`: 의존성 업데이트, 설정/문서 정리 등 기능·버그와 직접 무관한 작업. `main`에서 분기해 `main`으로 PR 머지 후, 필요 시 `main` → `release`.

#### 도표로 정리하면 다음과 같다  

| 브랜치 | 역할 | 배포 대상 |
| --- | --- | --- |
| `release` | 운영 배포 기준 브랜치 | 운영 |
| `main` | 스테이징/테스트 서버 기준 브랜치 | 스테이징 |
| `feature/*` | 신규 기능 개발 | 없음 (PR→`main`) |
| `fix/*` | 스테이징에서 발견된 버그 수정 | 없음 (PR→`main`) |
| `hotfix/*` | 운영 긴급 수정 (`release`에서 분기) | 운영(선반영), 이후 스테이징 동기화 |
| `chore/*` | 의존성/설정/문서 정리 등 | 없음 (PR→`main`) |

## 기본 흐름 (스테이징 → 운영)
1. 최신 `main`에서 작업 브랜치를 분기한다.  
   예) `git switch main && git pull && git switch -c feature/login`
2. 작업 후 PR을 `main`에 올리고 코드 리뷰와 테스트를 거쳐 머지한다.
3. 스테이징에서 검증 완료 후 운영 배포 시점에 `main`을 `release`로 머지한다.  
   - 방법: PR(`main` → `release`) 또는 `git switch release && git pull && git merge main`. (PR 방식 권장)
4. 운영 배포 후 태그가 필요하면 `release`에서 버전 태그를 만든다.  
   예) `git tag v1.2.0 && git push origin v1.2.0`

## 핫픽스 흐름 (운영 긴급 수정)
1. `release`에서 `hotfix/*` 브랜치를 분기한다.  
   예) `git switch release && git pull && git switch -c hotfix/critical-bug`
2. 수정 후 PR을 `release`에 올려 검토 및 테스트 후 머지한다.
3. 운영 배포 후 `release`를 `main`에 역병합해 코드 동기화를 유지한다.  
   예) `git switch main && git merge release`

## 브랜치 네이밍 규칙
- 접두어/짧은-설명 형태를 권장:  
  - `feature/login-ui`  
  - `fix/null-pointer`  
  - `hotfix/payment-timeout`  
  - `chore/upgrade-deps`

## 사용 원칙
- `main`, `release`에 직접 커밋하지 않는다. 모든 변경은 작업 브랜치 → PR → 리뷰 → 머지 순서로 진행한다.
- 브랜치 보호 권장 설정: 최소 1명 리뷰 승인, CI 통과 필수, force-push/직접 커밋 금지, `main`/`release`는 PR 머지만 허용(선호하는 머지 전략: squash 또는 merge commit).
- PR에는 작업 목적, 주요 변경점, 테스트 결과를 명시한다.
- 변경이 크거나 배포 영향이 있는 경우 스테이징에서 충분히 검증 후 `release`로 올린다.
- `fix`와 `hotfix`를 구분해 사용한다: 스테이징 발견 이슈는 `fix`, 운영 긴급 이슈는 `hotfix`.
- 배포 후에는 `main`과 `release`가 항상 동기화되도록 상호 머지한다.
