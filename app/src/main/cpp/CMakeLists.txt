cmake_minimum_required(VERSION 3.31.6)

include_directories(
        ${CMAKE_SOURCE_DIR}/include
        ${CMAKE_SOURCE_DIR}/src
)

set(SOURCES
        src/root_checker.cpp
        src/root_beer_checker.cpp
        src/native_root_checker.cpp
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
        ${log_lib}
)