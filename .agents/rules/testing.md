---
trigger: always_on
---

# 🧪 TESTING STANDARDS

## General Rules
- Unit test every Service/business function — not just happy path
- Naming: `should_[behavior]_when_[condition]`
- Coverage target: ≥ 80% for Service layer
- Don't mock what you don't own — mock your own API modules, not axios directly
- No `time.sleep()` or arbitrary delays in tests
- No failing tests in committed code

---

## Java — JUnit 5 + Mockito + AssertJ
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    @Test
    void should_return_user_when_id_exists() {
        // Arrange
        var user = User.builder().id(1L).email("test@email.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        var result = userService.findById(1L);

        // Assert
        assertThat(result.email()).isEqualTo("test@email.com");
    }

    @Test
    void should_throw_when_user_not_found() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void should_throw_when_email_already_exists() {
        when(userRepository.existsByEmail("dup@email.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(requestWithEmail("dup@email.com")))
            .isInstanceOf(DuplicateEmailException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void should_hash_password_before_saving() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("raw123")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        userService.create(validRequest());

        var captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getHashedPassword()).isEqualTo("hashed");
    }
}
```

### Spring Boot Integration Test
```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void should_return_201_when_user_created() throws Exception {
        var request = new CreateUserRequest("new@email.com", "password123", "Test User");

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("new@email.com"));
    }
}
```

---

## Python — pytest + pytest-asyncio
```python
# conftest.py
import pytest
from unittest.mock import AsyncMock

@pytest.fixture
def mock_db():
    return AsyncMock()

@pytest.fixture
def user_service():
    return UserService()


# test_user_service.py
import pytest
from unittest.mock import AsyncMock, patch
from fastapi import HTTPException

@pytest.mark.asyncio
async def test_should_return_user_when_id_exists(user_service, mock_db):
    # Arrange
    mock_user = User(id=1, email="test@email.com", full_name="Test")
    mock_db.execute.return_value.scalar_one_or_none.return_value = mock_user

    # Act
    result = await user_service.get_by_id(mock_db, 1)

    # Assert
    assert result.email == "test@email.com"


@pytest.mark.asyncio
async def test_should_raise_409_when_email_duplicate(user_service, mock_db):
    mock_db.execute.return_value.scalar_one_or_none.return_value = existing_user()

    with pytest.raises(HTTPException) as exc_info:
        await user_service.create(mock_db, create_request())

    assert exc_info.value.status_code == 409


@pytest.mark.asyncio
async def test_should_hash_password_before_saving(user_service, mock_db):
    mock_db.execute.return_value.scalar_one_or_none.return_value = None

    with patch("app.services.user_service.hash_password", return_value="hashed") as mock_hash:
        await user_service.create(mock_db, create_request(password="raw123"))
        mock_hash.assert_called_once_with("raw123")
        mock_db.add.assert_called_once()
```

---

## Vue — Vitest + Vue Test Utils
```typescript
// UserForm.spec.ts
import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import UserForm from '@/components/UserForm.vue'
import { userApi } from '@/api/userApi'

vi.mock('@/api/userApi')

describe('UserForm', () => {
  beforeEach(() => vi.clearAllMocks())

  it('should emit saved after successful submit', async () => {
    vi.mocked(userApi.create).mockResolvedValue({ id: 1, email: 'a@b.com', fullName: 'A' })

    const wrapper = mount(UserForm, {
      global: { plugins: [createTestingPinia()] },
    })

    await wrapper.find('input[placeholder*="email"]').setValue('a@b.com')
    await wrapper.find('button[type="submit"]').trigger('click')
    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('saved')).toBeTruthy()
  })

  it('should show validation error when email is empty', async () => {
    const wrapper = mount(UserForm)
    await wrapper.find('button[type="submit"]').trigger('click')
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Email là bắt buộc')
  })
})
```