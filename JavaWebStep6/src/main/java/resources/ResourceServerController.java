package resources;


public class ResourceServerController implements ResourceServerControllerMBean
{
	private TestResourceI testResource;

	public ResourceServerController(TestResourceI testResource)
	{
		this.testResource = testResource;
	}

	@Override
	public String getName()
	{
		return testResource.getName();
	}

	@Override
	public int getAge()
	{
		return testResource.getAge();
	}
}
