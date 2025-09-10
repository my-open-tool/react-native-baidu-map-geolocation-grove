require 'json'

package = JSON.parse(File.read(File.join(__dir__, '..', 'package.json')))

Pod::Spec.new do |s|
  s.name         = package['name']
  s.version      = package['version']
  s.summary      = package['description']
  s.homepage     = package['homepage']
  s.license      = package['license']
  s.author       = package['author']
  s.platform     = :ios, "11.0"
  s.source       = { :git => "https://github.com/zkgaojianjian/react-native-bmap-geolocation.git", :tag => "#{s.version}" }

  s.source_files = "*.{h,m}"
  s.requires_arc = true

  s.dependency "React-Core"
  s.frameworks = "CoreLocation"
end 