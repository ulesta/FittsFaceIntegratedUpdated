package ca.yorku.cse.mack.fittstouch;

class TracePoint
{
	int x, y, t;
	float tilt;

	TracePoint(int xArg, int yArg, int tArg, float tiltArg)
	{
		x = xArg;
		y = yArg;
		t = tArg;
		tilt = tiltArg;
	}
}
