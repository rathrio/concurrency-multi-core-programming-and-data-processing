#!/usr/bin/env ruby

# Script used to generate the requested report.

def col(str)
  str.to_s.ljust(16)
end

header = "#{col('Class')} | #{col('Threads')} | #{col('Runtime ms')} | #{col('Speedup')}"
puts header
puts '-' * header.length

[1, 2, 4, 8].each do |t|
  output = `java Ex1NoSync #{t} #{t} 100000`
  ms_nosync = output[/(\d+)(?=ns)/].to_f / 1_000_000

  puts "#{col('Ex1NoSync')} | #{col(t)} | #{col(ms_nosync.round(3))}"

  %w(Ex1Sync Ex1ReentrantLock).each do |klass|
    output = `java #{klass} #{t} #{t} 100000`
    ms = output[/(\d+)(?=ns)/].to_f / 1_000_000
    speed_up = (ms_nosync / ms).round(3)
    puts "#{col(klass)} | #{col(t)} | #{col(ms.round(3))} | #{col(speed_up)}"
  end
end
