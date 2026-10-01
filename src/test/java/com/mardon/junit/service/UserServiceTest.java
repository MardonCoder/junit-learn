package com.mardon.junit.service;

import com.mardon.junit.TestBase;
import com.mardon.junit.dao.UserDao;
import com.mardon.junit.dto.User;
import com.mardon.junit.extension.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("fast")
@Tag("user")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.DisplayName.class)
@ExtendWith({
        UserServiceParamResolver.class,
//        PostProcessingExtension.class,
        ConditionalExtension.class,
//        ThrowableExtension.class
//        GlobalExtension.class
        MockitoExtension.class
})
public class UserServiceTest extends TestBase {

    private static final User PETR = User.of(2, "Petr", "111");
    private static final User IVAN = User.of(1, "Ivan", "123");

    @Captor
    private ArgumentCaptor<Integer> argumentCaptor;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private UserDao userDao;
    @InjectMocks
    private UserService userService;

    UserServiceTest(TestInfo testInfo) {
        System.out.println();
    }

    @BeforeAll
    void init(){
        System.out.println("Before all: ");
    }

//    @BeforeEach
//    void prepare(UserService userService){
//        System.out.println("Before each: " + this);
//        this.userService = userService;
//    }

    @BeforeEach
    void prepare(){
        doReturn(true).when(this.userDao).delete(IVAN.getId()); // stub
//        lenient().when(userDao.delete(IVAN.getId())).thenReturn(true);
//        Mockito.mock(UserDao.class, withSettings().strictness(Strictness.LENIENT ));
//        this.userDao = Mockito.mock(UserDao.class);
//        this.userDao = Mockito.spy(new UserDao());
//        this.userService = new UserService(userDao);
    }

    @Test
    void throwExceptionIfDatabaseIsNotAvailable(){
        doThrow(RuntimeException.class).when(userDao).delete(IVAN.getId());
        assertThrows(RuntimeException.class, () -> userService.delete(IVAN.getId()));
    }

    @Test
    void shouldDeleteExistedUser() {
        userService.add(IVAN);
        doReturn(true).when(this.userDao).delete(IVAN.getId()); // stub
//        Mockito.doReturn(true).when(this.userDao).delete(Mockito.any());

//        Mockito.when(userDao.delete(IVAN.getId())).thenReturn(true).thenReturn(false);

        var deleteResult = userService.delete(IVAN.getId());
        System.out.println(userDao.delete(IVAN.getId()));
        System.out.println(userDao.delete(IVAN.getId()));

//        var integerArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(userDao, Mockito.atLeast(2)).delete(argumentCaptor.capture());
//        Mockito.verifyNoInteractions();
        assertThat(argumentCaptor.getValue()).isEqualTo(IVAN.getId());

//        Mockito.reset(userDao);

        assertThat(deleteResult).isTrue();
    }

    @Test
    @Order(1)
    @DisplayName("users will be empty if no user added")
    void usersEmptyIfNoUserAdded(){
        System.out.println("Test 1: " + this);
        var users = userService.getAll();
        assertTrue(users.isEmpty());
    }

    @Test
    @Order(2)
    void usersSizeIfAdded(){
        System.out.println("Test 2: " + this);
        userService.add(IVAN);
        userService.add(PETR);

        var users = userService.getAll();

        assertThat(users).hasSize(2);
//        assertEquals(2, users.size());
    }

    @Test
    void usersConvertedToMapById(){
        userService.add(IVAN, PETR);

        Map<Integer, User> users = userService.getAllConvertedById();

        assertAll(
                () -> assertThat(users).containsKeys(IVAN.getId(), PETR.getId()),
                () -> assertThat(users).containsValues(IVAN, PETR)
        );

    }

    @AfterEach
    void deleteDataFromDatabase(){
        System.out.println("After each: " + this);
    }

    @AfterAll
    void closeConnectionPool(){
        System.out.println("After all: ");
    }

    @Nested
    @DisplayName("tests user login functionality")
    @Tag("login")
    class LoginTest {

        @Test
        @Disabled("flaky, need to see")
        void loginFailIfPasswordIsNotCorrect(){
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login(IVAN.getUsername(), "NOT CORRECT PASSWORD");
            assertFalse(maybeUser.isPresent());
        }

        @RepeatedTest(value = 10, name = RepeatedTest.LONG_DISPLAY_NAME)
        void loginFailIfUserDoesNotExist(RepetitionInfo repetitionInfo){
            Optional<User> maybeUser = userService.login("dummy", "dummy");
            assertTrue(maybeUser.isEmpty());
        }

        @Test
        @Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
        void checkLoginFunctionalityPerformance() {
            var result = assertTimeout(Duration.ofMillis(200L),
                    () -> userService.login("dummy", IVAN.getPassword()));

        }

        @Test
        void loginSuccessIfUserExists(){
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login(IVAN.getUsername(), IVAN.getPassword());

            assertThat(maybeUser).isPresent();
            maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
//        assertTrue(maybeUser.isPresent());
//        maybeUser.ifPresent(user -> assertEquals(IVAN, user));
        }

        @Test
        void throwExceptionIfUserNameOrPasswordIsNull(){
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> userService.login(null, "dummy")),
                    () -> assertThrows(IllegalArgumentException.class, () -> userService.login("dummy", null))
            );
        }

        @ParameterizedTest(name = "{arguments} test")
//        @ArgumentsSource()
//        @NullSource
//        @EmptySource
//        @NullAndEmptySource
//        @ValueSource(strings = {
//                "Ivan", "Petr"
//        })
//        @EnumSource
        @MethodSource("com.mardon.junit.service.UserServiceTest#getArgumentsForLoginTest")
//        @CsvFileSource(resources = "/login-test-data.csv", delimiter = ',',numLinesToSkip = 1)
//        @CsvSource({
//                "Ivan,123",
//                "Petr,111"
//        })
        @DisplayName("login param test")
        void loginParametrizedTest(String username, String password, Optional<User> user) {
            userService.add(IVAN, PETR);

            var maybeUser = userService.login(username, password);
            assertThat(maybeUser).isEqualTo(user);
        }
    }

    static Stream<Arguments> getArgumentsForLoginTest(){
        return Stream.of(
                Arguments.of("Ivan", "123", Optional.of(IVAN)),
                Arguments.of("Petr", "111", Optional.of(PETR)),
                Arguments.of("Petr", "dummy", Optional.empty()),
                Arguments.of("dummy", "123", Optional.empty())
        );
    }
}
