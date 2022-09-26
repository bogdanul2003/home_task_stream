# How to build & run
The app should be compiled and run with java 17 and expects two parameters as input

Usage: app [companies_file] [news_folder]

At the end of the run it will output the number of companies found and how long it took. To get a better average you'll have to run it a few times.

"Parallel found 5096 companies in 9071 ms"

# Architecture
The app consists of two custom ForkJoin thread pool and a BlockingQueue which is used to communicate between the two pools.

XmlFilesProcessor uses a smaller thread pool to read news files in parallel as fast as possible and submits news to the BlockingQueue.

NewsProcessor uses a thread pool with number of cpus + 1 workers and waits for news from BlockingQueue. It basically does a map/reduce operation on the news.

Information about the companies is stored in Trie data structure that allows a fast lookup.
