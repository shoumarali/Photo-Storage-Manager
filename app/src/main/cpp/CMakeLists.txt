cmake_minimum_required(VERSION 3.31.6)

include_directories(${CMAKE_SOURCE_DIR}/include)

file(GLOB SOURCES src/*.cpp)

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