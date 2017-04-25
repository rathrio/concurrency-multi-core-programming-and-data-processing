#include <pthread.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>

#define ITERATIONS 100000000UL

int *data;
int padding = 0;

void *test(void *v)
{
	unsigned long i;
	int n = (int)(intptr_t)v;
	n *= (1 + padding);
	for (i = 0; i < ITERATIONS; i++)
		data[n]++;
	return NULL;
}

int main(int argc, char **argv)
{
	pthread_t *threads;
	pthread_attr_t attr;
	struct timeval starttime;
	struct timeval endtime;
	int max_threads;
	int i, n;
	double time;

	if (argc < 2) {
		fprintf(stderr, "Usage: %s max-threads [padding]\n", argv[0]);
		exit(1);
	}
	max_threads = atoi(argv[1]);
	if (argc > 2)
		padding = atoi(argv[2]);

	threads = (pthread_t *)malloc(max_threads * sizeof(pthread_t));
	data = (int *)malloc(max_threads * (sizeof(int) * (1 + padding)));

	pthread_attr_init(&attr);
	pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);
	for (n = 1; n <= max_threads; n++) {
		gettimeofday(&starttime, NULL);
		for (i = 0; i < n; i++) {
			if (pthread_create(&threads[i], &attr, test, (void *)(intptr_t)i) != 0)
				exit(1);
		}
		for (i = 0; i < n; i++) {
			if (pthread_join(threads[i], NULL) != 0)
				exit(1);
		}
		gettimeofday(&endtime, NULL);
		time = ((endtime.tv_sec - starttime.tv_sec) * 1000.0) + ((endtime.tv_usec - starttime.tv_usec) / 1000.0);
		printf("%d:\t%f\n", n, time);
	}
	pthread_attr_destroy(&attr);

	free(data);
	free(threads);

	return 0;
}
