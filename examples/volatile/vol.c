#include <stdio.h>

int main()
{
	volatile long i;
	volatile long k = 0;

	for (i = 0; i < 100000000; i++) {
		k += i;
	}
	printf("%ld\n", k);

	return 0;
}