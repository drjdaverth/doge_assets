Video.js HTTP Live Streaming
============================

HTTP Live Streaming is developer by Apply to allow iOS devices like the iPad,
iPod Touch or iPhone to select a video stream that suits the device capabilities
and available bandwidth. Each stream is segmented in multiple MPEG stream files
and has an m3u8 index files. Another m3u8 file lists these m3u8 files together
with the bitrate of these alternatives. This m3u8 master index file is used
by the iOS video player to select the right m3u8 file. When the available
bandwidth changes during playback, the device may switch to another stream.

This module intercepts m3u8 files supplied to the Video.js module.
It replaces these files with one new dynamically generated file file that makes
bandwidth switching available to iOS devices.

Requirements
------------

1. Multiple m3u8 files need to be supplied to Video.js. When just one m3u8 file
   is supplied there is no choice for the iOS player, so no master index is
   needed.
2. The files need to have the filemime application/vnd.apple.mpegurl.
3. The filenames need to contain `<number>k` in the file name, such as
   `sample-640k.m3u8`. This number is used to indicate the bandwidth
   to the client.

You can use the Video module with the Zencoder transcoder to create files
that are compatible with the Video.js module.

Configuration
-------------

The module works out of the box without configuration, provided you meet the
requirements and Video.js is working correctly.
By default, the m3u8 master index files are created dynamically: the paths
to the individual files are embedded in the path of the index file. This works
in most of the times and only breaks if the paths to the m3u8 files are very
long.
In those cases, you can change the `Delivery mode` to `Static files` in
the Video.js settings page. Now, the m3u8 master index file will be stored on
a configurable location. The filename will be formed by the MD5 hash of the
file contents, so a new file will only be written if the source file names
change. The master index file will never be deleted, so it is advisable to
write the master index files to a dedicated directoryso they can be removed
occasionally. It is no problem to remove a m3u8 master index file because they
will be recreated when needed.


Also see
--------

- https://app.zencoder.com/docs/guides/encoding-settings/http-live-streaming
- https://developer.apple.com/resources/http-streaming/
- http://drupal.org/project/video
