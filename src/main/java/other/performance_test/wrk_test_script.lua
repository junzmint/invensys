local socket = require("socket")
local counter = 1
local threads = {}

-- Get the current time in a format suitable for filenames
local function get_time_for_filename()
    return os.date("%Y%m%d_%H%M%S")
end

-- Create log file paths with current time
local log_file_path = string.format("test_result/req_res_log_%s.csv", get_time_for_filename())
local result_file_path = string.format("test_result/wrk_result_%s.csv", get_time_for_filename())

-- Function to open the log file and write the header if it's empty
local function open_log_file()
    local file = io.open(log_file_path, "a+")
    local size = file:seek("end")
    if size == 0 then
        file:write("Thread ID,Request Number,Request Time,Response Time\n")
    end
    file:close()
end

-- Function to log request and response times
local function log_times(id, request_num, req_time, res_time)
    local file = io.open(log_file_path, "a")
    file:write(string.format("%d,%d,%.6f,%.6f\n", id, request_num, req_time * 1000, res_time * 1000))
    file:close()
end

-- Open the log file and write the header
open_log_file()

-- Load the CSV file and parse it
local function load_csv(file)
    local f = assert(io.open(file, "r"))
    local content = f:read("*all")
    f:close()
    local lines = {}
    for line in content:gmatch("[^\r\n]+") do
        table.insert(lines, line)
    end
    return lines
end

-- Load the CSV data
local data_lines = load_csv("test_data/json_data.csv")
local data_count = #data_lines

-- Function to select a random line from the CSV
local function get_random_data()
    local line = data_lines[math.random(data_count)]
    return line
end

-- WRK setup
wrk.method = "POST"

-- Function to be called when setting up each thread
function setup(thread)
   thread:set("id", counter)
   table.insert(threads, thread)
   counter = counter + 1
end

-- delay before each request
function delay()
   return 0
end

-- Function to initialize each thread
function init(args)
    requests = 0
    responses = 0
    start_times = {} -- Table to store start times for each request
    first_request_skipped = false -- Flag to skip the first request for thread 1

    local msg = "Thread %d created"
    print(msg:format(id))
end

-- Function to generate requests
function request()
    -- Skip the first request for thread 1
    if id == 1 and not first_request_skipped then
        first_request_skipped = true
        -- print("Thread 1 is skipping the first request")
        return wrk.format(nil, "/inventory") -- Send an empty request to skip it
    end

    requests = requests + 1
    local body = get_random_data()
    wrk.body = body
    local req_start_time = socket.gettime() -- Record the start time using socket.gettime()
    start_times[requests] = req_start_time
    -- print("Thread " .. id .. " is making request number " .. requests .. " at time " .. req_start_time * 1000 .. " ms")
    return wrk.format(nil, "/inventory")
end

-- Function to process responses
function response(status, headers, body)
    responses = responses + 1
    local end_time = socket.gettime() -- Record the end time using socket.gettime()
    local req_start_time = start_times[responses]
    if req_start_time then
        log_times(id, responses, req_start_time, end_time) -- Log request and response times
        -- print("Thread " .. id .. " received response number " .. responses .. " at time " .. end_time * 1000 .. " ms")
    end
end

-- Function to summarize results
function done(summary, latency, requests)
    local result_file = io.open(result_file_path, "w")
    result_file:write("Thread ID,Requests,Responses\n")

    local total_requests = 0
    local total_responses = 0

    for index, thread in ipairs(threads) do
        local id = thread:get("id")
        local thread_requests = thread:get("requests")
        local thread_responses = thread:get("responses")
        total_requests = total_requests + thread_requests
        total_responses = total_responses + thread_responses
        local line = string.format("%d,%d,%d\n", id, thread_requests, thread_responses)
        result_file:write(line)
        local msg = "Thread %d made %d requests and got %d responses"
        print(msg:format(id, thread_requests, thread_responses))
    end

    result_file:close()
end