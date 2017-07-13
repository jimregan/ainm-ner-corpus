#!/usr/bin/perl

use warnings;
use strict;
use utf8;

binmode(STDIN, ":utf8");
binmode(STDOUT, ":utf8");

while(<>) {
    s!<persName ([^>]*)>([^<]*)<hide>([^<]*)</hide></persName>!<persName $1>$2</persName><hide>$3</hide>!g;
    s!<persName ([^>]*)>([^<]*)<placeName ([^>]*)>([^<]*)</placeName></persName>!<persName $1>$2$4</persName>!g;

    print;
}
