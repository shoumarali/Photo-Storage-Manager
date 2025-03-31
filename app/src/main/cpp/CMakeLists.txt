cmake_minimum_required(VERSION 3.31.6)

project(RootChecker)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

include_directories(
        ${CMAKE_SOURCE_DIR}/include
        ${CMAKE_SOURCE_DIR}/src
)

set(SOURCES
        src/root_checker.cpp
        src/root_beer_checker.cpp
        src/native_root_checker.cpp
        src/native_system_integrity_verifier.cpp
)

add_library(
        root_checker
        SHARED
        ${SOURCES}
)

find_library(
        log_lib
        log
)

target_link_libraries(
        root_checker
        PRIVATE
        ${log_lib}
)

if(ANDROID)
    target_link_libraries(root_checker PRIVATE -latomic)
    target_compile_options(root_checker PRIVATE -fvisibility=hidden)
endif()