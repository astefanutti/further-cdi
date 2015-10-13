require 'erb'
require 'asciidoctor'
require 'asciidoctor/cli'
require 'asciidoctor-diagram'
require 'tilt/asciidoc'

invoker = Asciidoctor::Cli::Invoker.new(%W(-T asciidoctor-backends/slim slides.adoc))

invoker.invoke!

  guard :shell do
    watch (/^.+\.adoc$/) { |m| invoker.invoke! }
  end

  guard 'livereload' do
    watch(%r{.+\.(css|js|html?|php|inc|theme)$})
  end
